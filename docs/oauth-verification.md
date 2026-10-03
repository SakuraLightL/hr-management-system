# 実Google OAuthの検証手順

**現状：実Googleアカウントを使った接続は未検証です。** 認可判断のユニットテスト成功は、Googleへのリダイレクト、コールバック、セッション確立の成功を証明しません。この文書は実施手順であり、完了報告ではありません。

## 設定

1. Google CloudのOAuthクライアントをWebアプリケーション用に用意します。同意画面がテスト状態の場合、使用するGoogleアカウントをテストユーザーとして登録します。
2. 承認済みリダイレクトURIに`http://localhost:8080/login/oauth2/code/google`を設定します。検証では`127.0.0.1`に切り替えず、ブラウザで`http://localhost:8080`を使用します。
3. `GOOGLE_CLIENT_ID`と`GOOGLE_CLIENT_SECRET`を環境変数に設定し、Java起動では`local,oauth`を指定します。

```sh
./mvnw spring-boot:run -Dspring-boot.run.profiles=local,oauth
```

Dockerでは手元の`.env`に2つの認証情報を設定し、`SPRING_PROFILES_ACTIVE=docker,oauth`に変更して`docker compose up --build --detach`を実行します。通常の`docker`プロファイルはGoogle認証情報なしで起動する設定です。

4. LOCALのADMINでログインし、ユーザー画面から認証方式GOOGLE、メール、固有のユーザー名、適切な権限を事前登録します。Googleと同じメールのLOCALユーザーが既にある場合、別のテストアカウントを使います。

認証情報、Cookie、トークン、実メールをGitや検証画像に保存しないでください。検証記録はアカウントA／Bなどの識別子を使用します。

## 確認表

| ケース | 期待する結果 | 実測結果 |
| --- | --- | --- |
| 事前登録済みGOOGLE／USERの初回ログイン | dashboardへ遷移。閲覧可、API更新とアカウント管理は拒否 | 未実施 |
| 同じアカウントの再ログイン | 固定済みsubjectでログイン。ユーザーが増えない | 未実施 |
| 事前登録済みGOOGLE／ADMIN | 管理画面とCSRF付きの更新操作が可能 | 未実施 |
| Google側では利用可能だがアプリには未登録 | 自動登録せずログインを拒否。Google用の失敗案内を表示 | 未実施 |
| 同じメールがLOCALとして登録済み | 自動連携せず拒否。LOCALログインは維持 | 未実施 |
| 同意画面をキャンセル | アプリへ戻れる場合、Google用の失敗案内を表示 | 未実施 |
| oauthプロファイルを外して再起動 | Googleボタンを表示せず、LOCALログイン可能 | 未実施 |

`email_verified=false`、subject不一致など、実Googleで任意に作りにくい異常条件は既存の`CustomOidcUserServiceTest`で検証します。Googleによって拒否されたケースと、アプリの事前登録ルールによる拒否を区別してください。

`GoogleLoginFlowTest`はダミーの認証情報でoauthプロファイルを起動し、認証開始時のstateと、同じセッションでのキャンセルコールバックが専用案内へ遷移することを確認します。Googleへのネットワーク接続、トークン交換、実アカウントでの成功ログインは行いません。

## 完了記録

実施後にのみ、日付、コミットSHA、実行プロファイル、ブラウザ、各ケースの実測結果を追記し、[検証状況](verification.md)とREADMEの状態を更新します。失敗した場合は期待値を書き換えず、修正と再確認の結果を記録します。
