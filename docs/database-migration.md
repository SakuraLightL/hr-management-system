# 既存DBからの移行

新規DBには操作不要です。既存DBを使う場合だけ、以下をメンテナンス時間に実施してください。

1. アプリを停止し、DBとDockerボリュームのバックアップを取得します。テスト用コピーで復元できることを確認します。
2. V1の3テーブル構成と既存スキーマを比較します。テーブル・列型が異なる場合はbaseline前に合わせてください。
3. 次のSQLで、ユーザー名重複と不正な権限・認証方式を確認します。該当データは管理者が正しい値を判断して修正してください。自動削除や任意の管理者降格は行いません。

```sql
SELECT username, COUNT(*) FROM users GROUP BY username HAVING COUNT(*) > 1;
SELECT id, role, provider FROM users
WHERE role NOT IN ('ADMIN', 'USER')
   OR (provider IS NOT NULL AND provider NOT IN ('LOCAL', 'GOOGLE'));
```

4. 過去のGoogle自動登録アカウントを確認し、承認していないアカウントを運用上整理してください。V2は既存の認可判断を勝手に変更しません。
5. 確認済みの既存DBに限り、一度だけ環境変数 `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` と `SPRING_FLYWAY_BASELINE_VERSION=1` を設定して新しいアプリを起動します。これにより既存スキーマをV1として登録し、V2がユーザー名の一意制約・HR列・監査列を追加します。
6. 成功後はbaseline設定を削除します。通常運用は `baseline-on-migrate=false` / `ddl-auto=validate` のままにします。

既存の `mysql-data` ボリューム名を維持しますが、Composeプロジェクト名や保存場所を変えると別ボリュームになるため、更新前に `docker volume ls` で確認してください。既存MySQLボリュームでは `MYSQL_USER` / `MYSQL_PASSWORD` の変更でDBアカウントは自動作成・更新されません。管理者が `hr_app` の作成・権限付与・パスワード変更を行い、接続設定を合わせてください。

旧Composeに保存されていたDBパスワードが現在も使われている場合は変更してください。現在の設定から除去しても、過去のGit履歴には残ります。

社員の削除は論理削除へ変更されます。削除済み行のメールも予約済みとして扱い、再登録で別社員に割り当てません。既存の物理削除済みレコードを復元することはできません。ロールバックはバックアップ復元で行ってください。旧バージョンは論理削除列を理解しないため、そのまま戻すと削除済み社員を表示する可能性があります。
