/*
 * Microsoft JDBC Driver for SQL Server Copyright(c) Microsoft Corporation All rights reserved. This program is made
 * available under the terms of the MIT License. See the LICENSE file in the project root for more information.
 */

package com.microsoft.sqlserver.jdbc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;


/**
 * Provides a simple implementation of the ISQLServerBulkRecord interface that can be used to read in the basic Java
 * data types from a delimited file where each line represents a row of data.
 */
public class SQLServerBulkCSVFileRecord extends SQLServerBulkCommon implements java.lang.AutoCloseable {
    /**
     * Update serialVersionUID when making changes to this file
     */
    private static final long serialVersionUID = 1546487135640225989L;
    /*
     * Resources associated with reading in the file
     */
    private BufferedReader fileReader;
    private InputStreamReader sr;
    private FileInputStream fis;

    /*
     * Current line of data to parse.
     */
    private String currentLine = null;

    /*
     * Delimiter to parse lines with.
     */
    private final String delimiter;

    /*
     * Class name for logging.
     */
    private static final String loggerClassName = "com.microsoft.sqlserver.jdbc.SQLServerBulkCSVFileRecord";

    /*
     * Logger
     */
    private static final java.util.logging.Logger loggerExternal = java.util.logging.Logger.getLogger(loggerClassName);

    /**
     * Constructs a simple reader to parse data from a delimited file with the given encoding.
     * 
     * @param fileToParse
     *        File to parse data from
     * @param encoding
     *        Charset encoding to use for reading the file, or NULL for the default encoding.
     * @param delimiter
     *        Delimiter to used to separate each column
     * @param firstLineIsColumnNames
     *        True if the first line of the file should be parsed as column names; false otherwise
     * @throws SQLServerException
     *         If the arguments are invalid, there are any errors in reading the file, or the file is empty
     */
    public SQLServerBulkCSVFileRecord(String fileToParse, String encoding, String delimiter,
            boolean firstLineIsColumnNames) throws SQLServerException {
        loggerExternal.entering(loggerClassName, "SQLServerBulkCSVFileRecord",
                new Object[] {fileToParse, encoding, delimiter, firstLineIsColumnNames});

        if (null == fileToParse) {
            throwInvalidArgument("fileToParse");
        } else if (null == delimiter) {
            throwInvalidArgument("delimiter");
        }

        this.delimiter = delimiter;
        try {
            // Create the file reader
            fis = new FileInputStream(fileToParse);
            if (null == encoding || 0 == encoding.length()) {
                sr = new InputStreamReader(fis);
            } else {
                sr = new InputStreamReader(fis, encoding);
            }

            fileReader = new BufferedReader(sr);

            if (firstLineIsColumnNames) {
                currentLine = fileReader.readLine();
                if (null != currentLine) {
                    columnNames = currentLine.split(delimiter, -1);
                }
            }
        } catch (UnsupportedEncodingException unsupportedEncoding) {
            MessageFormat form = new MessageFormat(SQLServerException.getErrString("R_unsupportedEncoding"));
            throw new SQLServerException(form.format(new Object[] {encoding}), null, 0, unsupportedEncoding);
        } catch (Exception e) {
            throw new SQLServerException(null, e.getMessage(), null, 0, false);
        }
        columnMetadata = new HashMap<>();

        loggerExternal.exiting(loggerClassName, "SQLServerBulkCSVFileRecord");
    }

    /**
     * Constructs a SQLServerBulkCSVFileRecord to parse data from a delimited file with the given encoding.
     * 
     * @param fileToParse
     *        InputStream to parse data from
     * @param encoding
     *        Charset encoding to use for reading the file, or NULL for the default encoding.
     * @param delimiter
     *        Delimiter to used to separate each column
     * @param firstLineIsColumnNames
     *        True if the first line of the file should be parsed as column names; false otherwise
     * @throws SQLServerException
     *         If the arguments are invalid, there are any errors in reading the file, or the file is empty
     */
    public SQLServerBulkCSVFileRecord(InputStream fileToParse, String encoding, String delimiter,
            boolean firstLineIsColumnNames) throws SQLServerException {
        loggerExternal.entering(loggerClassName, "SQLServerBulkCSVFileRecord",
                new Object[] {fileToParse, encoding, delimiter, firstLineIsColumnNames});

        if (null == fileToParse) {
            throwInvalidArgument("fileToParse");
        } else if (null == delimiter) {
            throwInvalidArgument("delimiter");
        }

        this.delimiter = delimiter;
        try {
            if (null == encoding || 0 == encoding.length()) {
                sr = new InputStreamReader(fileToParse);
            } else {
                sr = new InputStreamReader(fileToParse, encoding);
            }
            fileReader = new BufferedReader(sr);

            if (firstLineIsColumnNames) {
                currentLine = fileReader.readLine();
                if (null != currentLine) {
                    columnNames = currentLine.split(delimiter, -1);
                }
            }
        } catch (UnsupportedEncodingException unsupportedEncoding) {
            MessageFormat form = new MessageFormat(SQLServerException.getErrString("R_unsupportedEncoding"));
            throw new SQLServerException(form.format(new Object[] {encoding}), null, 0, unsupportedEncoding);
        } catch (Exception e) {
            throw new SQLServerException(null, e.getMessage(), null, 0, false);
        }
        columnMetadata = new HashMap<>();

        loggerExternal.exiting(loggerClassName, "SQLServerBulkCSVFileRecord");
    }

    /**
     * Constructs a SQLServerBulkCSVFileRecord to parse data from a CSV file with the given encoding.
     * 
     * @param fileToParse
     *        File to parse data from
     * @param encoding
     *        Charset encoding to use for reading the file.
     * @param firstLineIsColumnNames
     *        True if the first line of the file should be parsed as column names; false otherwise
     * @throws SQLServerException
     *         If the arguments are invalid, there are any errors in reading the file, or the file is empty
     */
    public SQLServerBulkCSVFileRecord(String fileToParse, String encoding,
            boolean firstLineIsColumnNames) throws SQLServerException {
        this(fileToParse, encoding, ",", firstLineIsColumnNames);
    }

    /**
     * Constructs a SQLServerBulkCSVFileRecord to parse data from a CSV file with the default encoding.
     * 
     * @param fileToParse
     *        File to parse data from
     * @param firstLineIsColumnNames
     *        True if the first line of the file should be parsed as column names; false otherwise
     * @throws SQLServerException
     *         If the arguments are invalid, there are any errors in reading the file, or the file is empty
     */
    public SQLServerBulkCSVFileRecord(String fileToParse, boolean firstLineIsColumnNames) throws SQLServerException {
        this(fileToParse, null, ",", firstLineIsColumnNames);
    }

    /**
     * Releases any resources associated with the file reader.
     * 
     * @throws SQLServerException
     *         when an error occurs
     */
    public void close() throws SQLServerException {
        loggerExternal.entering(loggerClassName, "close");

        // Ignore errors since we are only cleaning up here
        if (fileReader != null)
            try {
                fileReader.close();
            } catch (Exception e) {}
        if (sr != null)
            try {
                sr.close();
            } catch (Exception e) {}
        if (fis != null)
            try {
                fis.close();
            } catch (Exception e) {}

        loggerExternal.exiting(loggerClassName, "close");
    }

    @Override
    public DateTimeFormatter getColumnDateTimeFormatter(int column) {
        return columnMetadata.get(column).dateTimeFormatter;
    }

    @Override
    public Set<Integer> getColumnOrdinals() {
        return columnMetadata.keySet();
    }

    @Override
    public String getColumnName(int column) {
        return columnMetadata.get(column).columnName;
    }

    @Override
    public int getColumnType(int column) {
        return columnMetadata.get(column).columnType;
    }

    @Override
    public int getPrecision(int column) {
        return columnMetadata.get(column).precision;
    }

    @Override
    public int getScale(int column) {
        return columnMetadata.get(column).scale;
    }

    @Override
    public boolean isAutoIncrement(int column) {
        return false;
    }

    @Override
    public Object[] getRowData() throws SQLServerException {
        if (null == currentLine)
            return null;
        else {
            // Binary data may be corrupted
            // The limit in split() function should be a negative value,
            // otherwise trailing empty strings are discarded.
            // Empty string is returned if there is no value.
            String[] data = currentLine.split(delimiter, -1);

            // Cannot go directly from String[] to Object[] and expect it to act
            // as an array.
            Object[] dataRow = new Object[data.length];

            for (Entry<Integer, ColumnMetadata> pair : columnMetadata.entrySet()) {
                ColumnMetadata cm = pair.getValue();

                // Reading a column not available in csv
                // positionInFile > number of columns retrieved after split
                if (data.length < pair.getKey() - 1) {
                    MessageFormat form = new MessageFormat(SQLServerException.getErrString("R_invalidColumn"));
                    Object[] msgArgs = {pair.getKey()};
                    throw new SQLServerException(form.format(msgArgs), SQLState.COL_NOT_FOUND, DriverError.NOT_SET,
                            null);
                }

                // Source header has more columns than current line read
                if (columnNames != null && (columnNames.length > data.length)) {
                    MessageFormat form = new MessageFormat(SQLServerException.getErrString("R_DataSchemaMismatch"));
                    Object[] msgArgs = {};
                    throw new SQLServerException(form.format(msgArgs), SQLState.COL_NOT_FOUND, DriverError.NOT_SET,
                            null);
                }

                try {
                    if (0 == data[pair.getKey() - 1].length()) {
                        dataRow[pair.getKey() - 1] = null;
                        continue;
                    }

                    switch (cm.columnType) {
                        /*
                         * Both BCP and BULK INSERT considers double quotes as part of the data and throws error if any
                         * data (say "10") is to be inserted into an numeric column. Our implementation does the same.
                         */
                        case Types.INTEGER: {
                            // Formatter to remove the decimal part as SQL
                            // Server floors the decimal in integer types
                            DecimalFormat decimalFormatter = new DecimalFormat("#");
                            decimalFormatter.setRoundingMode(RoundingMode.DOWN);
                            String formatedfInput = decimalFormatter
                                    .format(Double.parseDouble(data[pair.getKey() - 1]));
                            dataRow[pair.getKey() - 1] = Integer.valueOf(formatedfInput);
                            break;
                        }

                        case Types.TINYINT:
                        case Types.SMALLINT: {
                            // Formatter to remove the decimal part as SQL
                            // Server floors the decimal in integer types
                            DecimalFormat decimalFormatter = new DecimalFormat("#");
                            decimalFormatter.setRoundingMode(RoundingMode.DOWN);
                            String formatedfInput = decimalFormatter
                                    .format(Double.parseDouble(data[pair.getKey() - 1]));
                            dataRow[pair.getKey() - 1] = Short.valueOf(formatedfInput);
                            break;
                        }

                        case Types.BIGINT: {
                            BigDecimal bd = new BigDecimal(data[pair.getKey() - 1].trim());
                            try {
                                dataRow[pair.getKey() - 1] = bd.setScale(0, RoundingMode.DOWN).longValueExact();
                            } catch (ArithmeticException ex) {
                                String value = "'" + data[pair.getKey() - 1] + "'";
                                MessageFormat form = new MessageFormat(
                                        SQLServerException.getErrString("R_errorConvertingValue"));
                                throw new SQLServerException(
                                        form.format(new Object[] {value, JDBCType.of(cm.columnType)}), null, 0, ex);
                            }
                            break;
                        }

                        case Types.DECIMAL:
                        case Types.NUMERIC: {
                            BigDecimal bd = new BigDecimal(data[pair.getKey() - 1].trim());
                            dataRow[pair.getKey() - 1] = bd.setScale(cm.scale, RoundingMode.HALF_UP);
                            break;
                        }

                        case Types.BIT: {
                            // "true" => 1, "false" => 0
                            // Any non-zero value (integer/double) => 1, 0/0.0
                            // => 0
                            try {
                                dataRow[pair.getKey()
                                        - 1] = (0 == Double.parseDouble(data[pair.getKey() - 1])) ? Boolean.FALSE
                                                                                                  : Boolean.TRUE;
                            } catch (NumberFormatException e) {
                                dataRow[pair.getKey() - 1] = Boolean.parseBoolean(data[pair.getKey() - 1]);
                            }
                            break;
                        }

                        case Types.REAL: {
                            dataRow[pair.getKey() - 1] = Float.parseFloat(data[pair.getKey() - 1]);
                            break;
                        }

                        case Types.DOUBLE: {
                            dataRow[pair.getKey() - 1] = Double.parseDouble(data[pair.getKey() - 1]);
                            break;
                        }

                        case Types.BINARY:
                        case Types.VARBINARY:
                        case Types.LONGVARBINARY:
                        case Types.BLOB: {
                            /*
                             * For binary data, the value in file may or may not have the '0x' prefix. We will try to
                             * match our implementation with 'BULK INSERT' except that we will allow 0x prefix whereas
                             * 'BULK INSERT' command does not allow 0x prefix. A BULK INSERT example: A sample csv file
                             * containing data for 2 binary columns and 1 row: 61,62 Table definition: create table
                             * t1(c1 varbinary(10), c2 varbinary(10)) BULK INSERT command: bulk insert t1 from
                             * 'C:\in.csv' with(DATAFILETYPE='char',firstrow=1, FIELDTERMINATOR=',') select * from t1
                             * shows 1 row with columns: 0x61, 0x62
                             */
                            // Strip off 0x if present.
                            String binData = data[pair.getKey() - 1].trim();
                            if (binData.startsWith("0x") || binData.startsWith("0X")) {
                                dataRow[pair.getKey() - 1] = binData.substring(2);
                            } else {
                                dataRow[pair.getKey() - 1] = binData;
                            }
                            break;
                        }

                        case java.sql.Types.TIME_WITH_TIMEZONE: {
                            OffsetTime offsetTimeValue;

                            // The per-column DateTimeFormatter gets priority.
                            if (null != cm.dateTimeFormatter)
                                offsetTimeValue = OffsetTime.parse(data[pair.getKey() - 1], cm.dateTimeFormatter);
                            else if (timeFormatter != null)
                                offsetTimeValue = OffsetTime.parse(data[pair.getKey() - 1], timeFormatter);
                            else
                                offsetTimeValue = OffsetTime.parse(data[pair.getKey() - 1]);

                            dataRow[pair.getKey() - 1] = offsetTimeValue;
                            break;
                        }

                        case java.sql.Types.TIMESTAMP_WITH_TIMEZONE: {
                            OffsetDateTime offsetDateTimeValue;

                            // The per-column DateTimeFormatter gets priority.
                            if (null != cm.dateTimeFormatter)
                                offsetDateTimeValue = OffsetDateTime.parse(data[pair.getKey() - 1],
                                        cm.dateTimeFormatter);
                            else if (dateTimeFormatter != null)
                                offsetDateTimeValue = OffsetDateTime.parse(data[pair.getKey() - 1], dateTimeFormatter);
                            else
                                offsetDateTimeValue = OffsetDateTime.parse(data[pair.getKey() - 1]);

                            dataRow[pair.getKey() - 1] = offsetDateTimeValue;
                            break;
                        }

                        case Types.NULL: {
                            dataRow[pair.getKey() - 1] = null;
                            break;
                        }

                        case Types.DATE:
                        case Types.CHAR:
                        case Types.NCHAR:
                        case Types.VARCHAR:
                        case Types.NVARCHAR:
                        case Types.LONGVARCHAR:
                        case Types.LONGNVARCHAR:
                        case Types.CLOB:
                        default: {
                            // The string is copied as is.
                            /*
                             * Handling double quotes: Both BCP (without a format file) and BULK INSERT behaves the same
                             * way for double quotes. They treat double quotes as part of the data. For a CSV file as
                             * follows, data is inserted as is: ""abc"" "abc" abc a"b"c a""b""c Excel on the other hand,
                             * shows data as follows. It strips off beginning and ending quotes, and sometimes quotes
                             * get messed up. When the same CSV is saved from Excel again, Excel adds additional quotes.
                             * abc"" abc abc a"b"c a""b""c In our implementation we will match the behavior with BCP and
                             * BULK INSERT. BCP command: bcp table1 in in.csv -c -t , -r 0x0A -S localhost -U sa -P
                             * <pwd> BULK INSERT command: bulk insert table1 from 'in.csv' with (FIELDTERMINATOR=',')
                             * Handling delimiters in data: Excel allows comma in data when data is surrounded with
                             * quotes. For example, "Hello, world" is treated as one cell. BCP and BULK INSERT deos not
                             * allow field terminators in data: https://technet.microsoft.com/en-us/library/
                             * aa196735%28v=sql.80%29.aspx?f=255&MSPPError=- 2147217396
                             */
                            dataRow[pair.getKey() - 1] = data[pair.getKey() - 1];
                            break;
                        }
                    }
                } catch (IllegalArgumentException e) {
                    String value = "'" + data[pair.getKey() - 1] + "'";
                    MessageFormat form = new MessageFormat(SQLServerException.getErrString("R_errorConvertingValue"));
                    throw new SQLServerException(form.format(new Object[] {value, JDBCType.of(cm.columnType)}), null, 0,
                            e);
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new SQLServerException(SQLServerException.getErrString("R_DataSchemaMismatch"), e);
                }

            }
            return dataRow;
        }
    }

    @Override
    void addColumnMetadataInternal(int positionInSource, String name, int jdbcType, int precision, int scale,
            DateTimeFormatter dateTimeFormatter) throws SQLServerException {
        loggerExternal.entering(loggerClassName, "addColumnMetadata",
                new Object[] {positionInSource, name, jdbcType, precision, scale});

        String colName = "";

        if (0 >= positionInSource) {
            MessageFormat form = new MessageFormat(SQLServerException.getErrString("R_invalidColumnOrdinal"));
            Object[] msgArgs = {positionInSource};
            throw new SQLServerException(form.format(msgArgs), SQLState.COL_NOT_FOUND, DriverError.NOT_SET, null);
        }

        if (null != name)
            colName = name.trim();
        else if ((null != columnNames) && (columnNames.length >= positionInSource))
            colName = columnNames[positionInSource - 1];

        if ((null != columnNames) && (positionInSource > columnNames.length)) {
            MessageFormat form = new MessageFormat(SQLServerException.getErrString("R_invalidColumn"));
            Object[] msgArgs = {positionInSource};
            throw new SQLServerException(form.format(msgArgs), SQLState.COL_NOT_FOUND, DriverError.NOT_SET, null);
        }

        checkDuplicateColumnName(positionInSource, name);
        switch (jdbcType) {
            /*
             * SQL Server supports numerous string literal formats for temporal types, hence sending them as varchar
             * with approximate precision(length) needed to send supported string literals. string literal formats
             * supported by temporal types are available in MSDN page on data types.
             */
            case java.sql.Types.DATE:
            case java.sql.Types.TIME:
            case java.sql.Types.TIMESTAMP:
            case microsoft.sql.Types.DATETIMEOFFSET:
                columnMetadata.put(positionInSource,
                        new ColumnMetadata(colName, jdbcType, 50, scale, dateTimeFormatter));
                break;

            // Redirect SQLXML as LONGNVARCHAR
            // SQLXML is not valid type in TDS
            case java.sql.Types.SQLXML:
                columnMetadata.put(positionInSource,
                        new ColumnMetadata(colName, java.sql.Types.LONGNVARCHAR, precision, scale, dateTimeFormatter));
                break;

            // Redirecting Float as Double based on data type mapping
            // https://msdn.microsoft.com/en-us/library/ms378878%28v=sql.110%29.aspx
            case java.sql.Types.FLOAT:
                columnMetadata.put(positionInSource,
                        new ColumnMetadata(colName, java.sql.Types.DOUBLE, precision, scale, dateTimeFormatter));
                break;

            // redirecting BOOLEAN as BIT
            case java.sql.Types.BOOLEAN:
                columnMetadata.put(positionInSource,
                        new ColumnMetadata(colName, java.sql.Types.BIT, precision, scale, dateTimeFormatter));
                break;

            default:
                columnMetadata.put(positionInSource,
                        new ColumnMetadata(colName, jdbcType, precision, scale, dateTimeFormatter));
        }

        loggerExternal.exiting(loggerClassName, "addColumnMetadata");
    }

    @Override
    public void setTimestampWithTimezoneFormat(String dateTimeFormat) {
        loggerExternal.entering(loggerClassName, "setTimestampWithTimezoneFormat", dateTimeFormat);

        super.setTimestampWithTimezoneFormat(dateTimeFormat);

        loggerExternal.exiting(loggerClassName, "setTimestampWithTimezoneFormat");
    }

    @Override
    public void setTimestampWithTimezoneFormat(DateTimeFormatter dateTimeFormatter) {
        if (loggerExternal.isLoggable(java.util.logging.Level.FINER)) {
            loggerExternal.entering(loggerClassName, "setTimestampWithTimezoneFormat",
                    new Object[] {dateTimeFormatter});
        }

        super.setTimestampWithTimezoneFormat(dateTimeFormatter);

        loggerExternal.exiting(loggerClassName, "setTimestampWithTimezoneFormat");
    }

    @Override
    public void setTimeWithTimezoneFormat(String timeFormat) {
        loggerExternal.entering(loggerClassName, "setTimeWithTimezoneFormat", timeFormat);

        super.setTimeWithTimezoneFormat(timeFormat);

        loggerExternal.exiting(loggerClassName, "setTimeWithTimezoneFormat");
    }

    @Override
    public void setTimeWithTimezoneFormat(DateTimeFormatter dateTimeFormatter) {
        if (loggerExternal.isLoggable(java.util.logging.Level.FINER)) {
            loggerExternal.entering(loggerClassName, "setTimeWithTimezoneFormat", new Object[] {dateTimeFormatter});
        }

        super.setTimeWithTimezoneFormat(dateTimeFormatter);

        loggerExternal.exiting(loggerClassName, "setTimeWithTimezoneFormat");
    }

    @Override
    public boolean next() throws SQLServerException {
        try {
            currentLine = fileReader.readLine();
        } catch (IOException e) {
            throw new SQLServerException(e.getMessage(), null, 0, e);
        }
        return (null != currentLine);
    }
}
