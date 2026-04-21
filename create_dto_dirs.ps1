$basePath = "C:\Users\moham\IdeaProjects\Aether-Bank\backend\iam-service\src\main\java\com\maayn\iamservice\dto"

# Create main DTO directory
New-Item -ItemType Directory -Force -Path $basePath | Out-Null

# Create subdirectories for different DTO categories
New-Item -ItemType Directory -Force -Path "$basePath\request" | Out-Null
New-Item -ItemType Directory -Force -Path "$basePath\response" | Out-Null
New-Item -ItemType Directory -Force -Path "$basePath\admin" | Out-Null
New-Item -ItemType Directory -Force -Path "$basePath\audit" | Out-Null

Write-Host "Directory structure created successfully!"
