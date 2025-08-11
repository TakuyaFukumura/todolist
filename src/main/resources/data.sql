DELETE FROM message;
INSERT INTO message (text) VALUES ('Hello World!');

DELETE FROM users;
-- user/password (BCrypt hash of "password")
INSERT INTO users (username, password, roles, enabled) VALUES
('user', '$2a$10$XY0d9WX4.TfZPJnYVN7/6OK5m8q0T8qZ8EfE6DZk6Fq1BT5v0vQ8G', 'USER', true);
INSERT INTO users (username, password, roles, enabled) VALUES
('admin', '$2a$10$ac1.Hor.5Q6r2vdRrQ9uxOo66w.G6YWJiXzArcGIkxKdzNKPrvQce', 'ADMIN,USER', true);

DELETE FROM todo;
-- Sample todos for user (user_id=1)
INSERT INTO todo (title, description, due_date, priority, status, created_at, updated_at, user_id) VALUES
('プロジェクト計画書作成', '来月のプロジェクトの基本計画書を作成する', '2025-08-15', 'HIGH', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
('会議資料準備', '定例会議用の資料を準備する', '2025-08-12', 'MEDIUM', 'IN_PROGRESS', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
('システム仕様書レビュー', '新システムの仕様書をレビューし、フィードバックを提供', '2025-08-20', 'HIGH', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
('チーム懇親会の企画', '四半期チーム懇親会の企画と準備', '2025-08-25', 'LOW', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1),
('完了済みタスク', '既に完了したテストタスク', '2025-08-10', 'MEDIUM', 'COMPLETED', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1);

-- Sample todos for admin (user_id=2)
INSERT INTO todo (title, description, due_date, priority, status, created_at, updated_at, user_id) VALUES
('サーバーメンテナンス', '月次サーバーメンテナンスの実施', '2025-08-18', 'HIGH', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2),
('セキュリティ監査', 'システムセキュリティの定期監査', '2025-08-30', 'HIGH', 'PENDING', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 2);
