# HR Management System

## 概要
Java + Spring Boot を用いた社員管理システム

## 機能
- ログイン機能（Spring Security）
- 社員CRUD
- 検索機能
- ユーザー管理
- REST API
- バリデーション
- エラーハンドリング

## 技術スタック
- Java
- Spring Boot
- MySQL
- Thymeleaf
- JavaScript（fetch API）

## API仕様

本アプリでは、社員情報管理機能をREST APIとして実装しています。  
API仕様はSwagger UIから確認できます。

### Swagger UI

http://localhost:8080/swagger-ui.html

### OpenAPI Docs

http://localhost:8080/v3/api-docs

## Employee API

| Method | Endpoint | Description |
|---|---|---|
| GET | /api/employees | 社員一覧取得 |
| POST | /api/employees | 社員登録 |
| PUT | /api/employees/{id} | 社員情報更新 |
| DELETE | /api/employees/{id} | 社員削除 |

## 特徴
- DTOによるデータ分離
- 共通レスポンス設計
- グローバル例外処理

## 今後の課題
- UI改善
- ページネーション
- AWSデプロイ