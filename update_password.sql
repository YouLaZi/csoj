UPDATE user SET userPassword = '$2a$10$/jbyJrLWBDvMBCe6a.VoTeyxzbS9XhOvm3oSAdlRm8pQJDElCVxJu' WHERE userAccount = 'admin';
SELECT id, userAccount, userPassword FROM user WHERE userAccount = 'admin';
