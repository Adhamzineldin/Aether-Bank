@echo off
setlocal enabledelayedexpansion

set "basePath=C:\Users\moham\IdeaProjects\Aether-Bank\backend\iam-service\src\main\java\com\maayn\iamservice\dto"

if not exist "!basePath!" mkdir "!basePath!"
if not exist "!basePath!\request" mkdir "!basePath!\request"
if not exist "!basePath!\response" mkdir "!basePath!\response"
if not exist "!basePath!\admin" mkdir "!basePath!\admin"
if not exist "!basePath!\audit" mkdir "!basePath!\audit"

echo Directory structure created successfully!
dir "!basePath!"
