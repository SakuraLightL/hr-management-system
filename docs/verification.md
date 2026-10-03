# 検証状況

## 改善前の公開mainで確認できる証拠

- 対象コミット：`5351c7bf46eaa10b5569bfadbe70ca6a97d65d56`。
- [GitHub Actions実行](https://github.com/SakuraLightL/hr-management-system/actions/runs/34743175422)：2026-09-13、Java 17のMaven verifyとNodeテストが成功。既存テストはJava27件、Node3件。
- 既存の設計文書にはH2とMySQL 8.0.45での移行・起動確認が記載されています。ただし、その手動実行を今回再現したわけではありません。

## 2026-10-03の改善作業

| 項目 | 状態 | 制約 |
| --- | --- | --- |
| 既存Node回帰テスト3件 | 成功 | Node標準テストランナー |
| スモーククライアントのPythonテスト2件 | 成功 | Cookie・CSRF、接続切断と初期管理者の起動待ちを確認 |
| Compose・CIのYAML構文 | 確認 | Docker実行を意味しない |
| Javaテスト29件（失敗案内・oauthプロファイルとキャンセルフローの検証を追加） | CI成功 | 作業環境ではMaven依存を取得できず、GitHub Actionsで確認 |
| Dockerビルド・MySQL・実セッション検証 | CI成功 | ログイン、CSRF、権限、登録・検索・更新・論理削除、再起動後の保持を確認 |
| 実Google OAuth | 未実施 | 手元のGoogle OAuth設定とアカウントが必要 |
| 公開デモ環境 | なし | ローカル手順と既存画面のGIFを提供 |

公開側の検証対象コミットは`fe6497ccbe0531b612b089835b2f4a88dc7d0d35`です。[公開CI実行](https://github.com/SakuraLightL/hr-management-system/actions/runs/37110632426)で、`test`と`docker-smoke`の両ジョブが成功しました。Java29件、Node3件、Python2件のテスト、DockerイメージとMySQL 8.4の起動、実LOCALセッションを使う操作、再起動後のデータ保持を確認しています。

初回の移行元スモーク検証は起動直後の接続切断で失敗したため、接続と初期管理者ログインの準備完了まで再試行する処理を追加しました。今回の公開CIでも、その修正を含めて確認しています。

この検証記録を更新するコミットは検証対象より後ですが、アプリ・テスト・CIの動作は変更していません。実Googleへの接続、トークン交換、実アカウントでの成功ログインは未検証です。ダミー認証情報でのキャンセルフロー成功と区別しています。

## Dockerスモークテスト

`scripts/smoke-test.py`はPython 3の標準ライブラリだけを使用します。既存H2テストに加えて、Dockerイメージ、MySQLでのFlyway適用、実LOCALログイン、セッション、CSRF、ADMIN／USERの認可、人事入力チェック、再起動後のデータ保持、論理削除を確認する構成です。Googleへの接続は行いません。

**専用の空DBとComposeプロジェクトで実行してください。** 作成した社員は論理削除されるため、テスト後も行が残ります。スクリプトは接続先をループバックに限定し、認証情報を標準出力へ出しません。

手元の専用クローンで`.env`を設定した後、以下を実行します。

```sh
export COMPOSE_PROJECT_NAME=hr-portfolio-smoke
docker compose up --build --detach
export SMOKE_ADMIN_USERNAME='admin'
# 手元の初期管理者パスワードをSMOKE_ADMIN_PASSWORDに設定する
python3 scripts/smoke-test.py flow
docker compose restart app
python3 scripts/smoke-test.py persisted
python3 scripts/smoke-test.py cleanup
docker compose down
```

初期管理者名を変更した場合は`SMOKE_ADMIN_USERNAME`も合わせます。`target/smoke-state.json`は再起動前後の確認対象IDだけを保存し、パスワードやCookieを含みません。CIは独立したプロジェクト名とランダムなパスワードを使い、終了時に専用ボリュームも削除します。

## 検証の範囲

- Nodeテストは簡易DOMでの回帰確認であり、実ブラウザの全操作を検証するものではありません。
- スモークテストは実HTTPを使いますが、JavaScriptの画面操作は実行しません。画面操作は[90秒のデモ手順](demo-guide.md)で確認します。
- OIDCの許可判断のユニットテストと[実Google検証](oauth-verification.md)は分けて管理します。
