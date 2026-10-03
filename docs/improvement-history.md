# レビューを受けた改善

初期実装に対する外部レビューを受け、次の問題を修正しました。過去のIssueとPRは非公開の履歴保管用リポジトリにあるため、この公開文書ではコードとテストから判断できる内容を記載します。

| 初期実装の問題 | 判断・修正 | 確認できる証拠 |
| --- | --- | --- |
| APIはログイン済みであれば更新できた | 閲覧はUSER、更新はADMIN。画面でボタンを隠すだけでなく、SecurityFilterChainでAPIも制限 | `SecurityConfig.java`、`HrSystemApplicationTests.java`のUSER更新拒否 |
| セッション認証なのにCSRFを無効化し、GETで削除していた | MVCとAPIのCSRFを有効化。削除はPOST／DELETE、fetchはページのトークンを送信 | `csrf.js`、CSRFなしの更新拒否・GET削除拒否テスト |
| APIの社員情報をHTMLとして組み立てていた | `textContent`で描画し、文字列をHTMLとして実行しない | `employees.js`、`src/test/js/employees.test.cjs`の保存型XSS回帰テスト |
| Googleのメールだけで自動登録・既存アカウント連携していた | GOOGLEの事前登録、検証済みメール、subject固定を要求。LOCALと自動連携しない | `CustomOidcUserService.java`とそのユニットテスト。実Google接続は別途検証が必要 |
| 起動時にDB変更を自動推測していた | FlywayのV1／V2で変更を管理し、JPAはvalidate。既存DBはバックアップとbaselineを経て移行 | `db/migration/`、[DB移行手順](database-migration.md) |

今回のREADME・Docker・OAuth検証の改善は[公開PR #1](https://github.com/SakuraLightL/hr-management-system/pull/1)で確認できます。実行結果は[検証状況](verification.md)から公開CIへ辿れます。

## 設計上の選択

- MVCとAPIのセッションを共有するため、両方で同じ認可・CSRFルールを適用しました。
- 社員の削除は論理削除とし、退職状態とは分けています。削除した社員のメールは再利用しません。
- 作成者・更新者と日時は追跡用メタデータです。変更前後を蓄積する監査台帳は実装していません。
- APIの`status/message/data`形式は互換性のため維持しました。可変フォームDTOもMVCのバインディングに合わせた選択です。

詳細は[設計方針](design-decisions.md)、現在の証拠と制約は[検証状況](verification.md)を参照してください。
