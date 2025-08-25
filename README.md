# Bank Transfer Processing System

A Java Core application for processing bank transfer transactions from text files with comprehensive validation and reporting.

## Features

- **File Processing**: Parses `.txt` files from the `input/` directory
- **Transaction Validation**: Validates account numbers (XXXXX-XXXXX format) and positive amounts
- **Balance Management**: Updates account balances after successful transfers
- **Comprehensive Reporting**: Logs all operations (both successful and failed) with detailed error messages
- **Date Filtering**: View transaction history filtered by date range
- **Automatic Archiving**: Moves processed files to `archive/` directory

## Input File Format

Processed files must be `.txt` format with the following structure:
```txt
from: XXXXX-XXXXX
to: XXXXX-XXXXX
amount: 100.50
optional: fields
```

## Usage
Place transaction files in the input directory

Run the application

Choose an option:

1 - Process files from input directory

2 - View all transactions from report

3 - View transactions by date range

## Output
Report File: archive/report.txt with all processed operations

Account Balances: Updated in accounts.txt

Archived Files: Processed files moved to archive directory

## Example Report Entry
2025-08-25 17:56:29 | transfer_1.txt | перевод с 10001-10003 на 10001-1000250.0 | SUCCESS | успешно обработан

2025-08-25 17:56:29 | transfer_1.txt | перевод с 10001-10001 на 10001-10002-100.5 | ERROR | неверная сумма перевода: -100.5
