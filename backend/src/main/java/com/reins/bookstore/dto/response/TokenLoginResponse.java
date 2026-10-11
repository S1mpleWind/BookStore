package com.reins.bookstore.dto.response;

/** 保留前端需要的用户字段，并返回访问凭证。 */
public record TokenLoginResponse(Long userId, String username, String nickname, Integer identity,
                                 String token, String tokenType, long expiresIn) {
    public static TokenLoginResponse of(UserLoginResponse user, String token, long expiresIn) {
        return new TokenLoginResponse(user.getUserId(), user.getUsername(), user.getNickname(),
                user.getIdentity(), token, "Bearer", expiresIn);
    }
}
