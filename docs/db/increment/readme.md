### Increment SQL for each version

File naming format:
```text
scan-v{previousVersion}-to-v{nextVersion}-{cleanData}.sql

$previousVersion: the previous version number
$nextVersion: next version number
$cleanData: Whether it is necessary to clear the database. If it is true, all table data under the database need to be cleared and then the initialization data is imported. If it is false, the script can be imported.
```
