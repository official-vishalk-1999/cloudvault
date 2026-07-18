# CloudVault

A file vault built on AWS. Binary files are stored in S3 and their metadata in DynamoDB. Users sign up and log in with BCrypt-hashed passwords. On read, the app returns a 10-minute presigned S3 URL for each file, so the bucket stays private and the browser loads images directly from S3.

## Stack

Spring Boot 3, Java 17, AWS SDK v2 (S3, DynamoDB), BCrypt.

## AWS resources

- S3 bucket (private) in `ap-south-1`
- DynamoDB table `Users` — partition key `email` (String)
- DynamoDB table `Images` — partition key `userId` (String), sort key `imageId` (String)

The composite key on `Images` lets the app read one user's files with a Query on `userId` instead of a full table Scan.

## Configuration

All AWS settings live in `src/main/resources/application.properties`. The two secret values are read from environment variables so no real keys are ever committed:

```
aws.region=ap-south-1
aws.access-key-id=${AWS_ACCESS_KEY_ID:}
aws.secret-access-key=${AWS_SECRET_ACCESS_KEY:}
aws.s3.bucket=${S3_BUCKET:REPLACE_WITH_YOUR_BUCKET_NAME}
aws.dynamodb.users-table=Users
aws.dynamodb.images-table=Images
```

Set these before running:

```
AWS_ACCESS_KEY_ID=...
AWS_SECRET_ACCESS_KEY=...
S3_BUCKET=your-bucket-name
```

## Run

```
mvn spring-boot:run
```

Open http://localhost:8080

## Endpoints

- `POST /api/auth/signup` — register a user
- `POST /api/auth/login` — verify credentials
- `POST /api/files/upload` — upload a file (`file`, `userId`)
- `GET /api/files/{userId}` — list a user's files with presigned URLs
