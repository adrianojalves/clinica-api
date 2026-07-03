package br.com.ajasoftware.clinica.service.backup;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for managing database backups.
 * Programmatically extracts all tables, structures (DDL), and records (DML).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BackupService {

    private final DataSource dataSource;

    /**
     * Generates a SQL script containing DROP TABLE, CREATE TABLE, and INSERT INTO statements
     * for all tables in the current schema/catalog. Disables foreign key checks during import
     * and excludes generated columns.
     */
    public String generateBackupSql() {
        StringBuilder sql = new StringBuilder();

        // SQL Header Comments
        sql.append("-- ======================================================\n");
        sql.append("-- SysClinica Database Backup\n");
        sql.append("-- Generated on: ").append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))).append("\n");
        sql.append("-- ======================================================\n\n");

        // Temporarily disable foreign key checks to prevent constraints violation during restoration
        sql.append("SET FOREIGN_KEY_CHECKS = 0;\n\n");

        try (Connection conn = dataSource.getConnection()) {
            String catalog = conn.getCatalog();
            DatabaseMetaData metaData = conn.getMetaData();

            // Retrieve all user tables for the current catalog
            List<String> tableNames = new ArrayList<>();
            try (ResultSet rs = metaData.getTables(catalog, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    String tableName = rs.getString("TABLE_NAME");
                    tableNames.add(tableName);
                }
            }

            for (String tableName : tableNames) {
                sql.append("-- ------------------------------------------------------\n");
                sql.append("-- Table structure and data for table `").append(tableName).append("`\n");
                sql.append("-- ------------------------------------------------------\n\n");

                // Drop table statement
                sql.append("DROP TABLE IF EXISTS `").append(tableName).append("`;\n");

                // Retrieve creation DDL (SHOW CREATE TABLE)
                try (Statement stmt = conn.createStatement();
                     ResultSet createTableRs = stmt.executeQuery("SHOW CREATE TABLE `" + tableName + "`")) {
                    if (createTableRs.next()) {
                        String createTableSql = createTableRs.getString(2);
                        sql.append(createTableSql).append(";\n\n");
                    }
                }

                // Get only writable columns to exclude generated/virtual columns
                List<String> writableColumns = getWritableColumns(conn, catalog, tableName);
                if (writableColumns.isEmpty()) {
                    // Fallback to all columns if metadata query fails
                    try (Statement stmt = conn.createStatement();
                         ResultSet tempRs = stmt.executeQuery("SELECT * FROM `" + tableName + "` LIMIT 0")) {
                        ResultSetMetaData tempMeta = tempRs.getMetaData();
                        for (int i = 1; i <= tempMeta.getColumnCount(); i++) {
                            writableColumns.add(tempMeta.getColumnName(i));
                        }
                    }
                }

                // Build columns list for SELECT and INSERT statements
                StringBuilder selectCols = new StringBuilder();
                StringBuilder insertCols = new StringBuilder();
                for (int i = 0; i < writableColumns.size(); i++) {
                    String col = writableColumns.get(i);
                    if (i > 0) {
                        selectCols.append(", ");
                        insertCols.append(", ");
                    }
                    selectCols.append("`").append(col).append("`");
                    insertCols.append("`").append(col).append("`");
                }

                String selectQuery = "SELECT " + selectCols + " FROM `" + tableName + "`";

                // Retrieve all rows using the constructed select query
                try (Statement stmt = conn.createStatement();
                     ResultSet dataRs = stmt.executeQuery(selectQuery)) {

                    ResultSetMetaData rsMetaData = dataRs.getMetaData();
                    int columnCount = rsMetaData.getColumnCount();

                    boolean hasData = false;
                    while (dataRs.next()) {
                        if (!hasData) {
                            sql.append("-- Dumping data for table `").append(tableName).append("`\n");
                            hasData = true;
                        }

                        StringBuilder values = new StringBuilder();
                        for (int i = 1; i <= columnCount; i++) {
                            if (i > 1) {
                                values.append(", ");
                            }
                            Object value = dataRs.getObject(i);
                            if (value == null) {
                                values.append("NULL");
                            } else if (value instanceof Number || value instanceof Boolean) {
                                if (value instanceof Boolean) {
                                    values.append((Boolean) value ? "1" : "0");
                                } else {
                                    values.append(value.toString());
                                }
                            } else {
                                // Escapes single quotes and backslashes
                                String strValue = value.toString();
                                String escaped = strValue.replace("\\", "\\\\").replace("'", "\\'");
                                values.append("'").append(escaped).append("'");
                            }
                        }

                        sql.append("INSERT INTO `").append(tableName).append("` (")
                                .append(insertCols).append(") VALUES (")
                                .append(values).append(");\n");
                    }

                    if (hasData) {
                        sql.append("\n");
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error generating database backup", e);
            throw new RuntimeException("Falha ao gerar o backup do banco de dados: " + e.getMessage(), e);
        }

        // Re-enable foreign key checks at the end
        sql.append("SET FOREIGN_KEY_CHECKS = 1;\n");

        return sql.toString();
    }

    /**
     * Queries database metadata from information_schema to retrieve all non-generated columns for a table.
     */
    private List<String> getWritableColumns(Connection conn, String catalog, String tableName) {
        List<String> columns = new ArrayList<>();
        String query = "SELECT COLUMN_NAME FROM information_schema.COLUMNS " +
                       "WHERE TABLE_SCHEMA = ? " +
                       "  AND TABLE_NAME = ? " +
                       "  AND EXTRA NOT LIKE '%GENERATED%' " +
                       "ORDER BY ORDINAL_POSITION";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setString(1, catalog);
            ps.setString(2, tableName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    columns.add(rs.getString("COLUMN_NAME"));
                }
            }
        } catch (Exception e) {
            log.error("Error fetching writable columns for table: " + tableName, e);
        }
        return columns;
    }
}
