package com.reins.bookstore;

import com.reins.bookstore.repository.UserAuthRepository;
import com.reins.bookstore.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/** 使用真实嵌入式 Tomcat 和 HTTP 请求验证鉴权与虚拟线程。 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("demo")
class JwtWebIntegrationTest {
    private static final SecretKey KEY = Jwts.SIG.HS256.key().build();

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("jwt.secret", () -> Encoders.BASE64.encode(KEY.getEncoded()));
    }

    @Autowired private TestRestTemplate http;
    @Autowired private JwtService jwt;
    @Autowired private UserAuthRepository users;

    @BeforeEach
    void useJavaHttpClient() {
        // HttpURLConnection 会移除 Origin，并且 POST 401 存在流式重试限制。
        http.getRestTemplate().setRequestFactory(new JdkClientHttpRequestFactory());
    }

    private ResponseEntity<Map> getBook(String authorization) {
        HttpHeaders headers = new HttpHeaders();
        if (authorization != null) headers.set(HttpHeaders.AUTHORIZATION, authorization);
        return http.exchange("/api/v1/books/1", HttpMethod.GET, new HttpEntity<>(headers), Map.class);
    }

    private String login(String username, String password) {
        ResponseEntity<Map> response = http.postForEntity("/api/v1/users/login",
                Map.of("username", username, "password", password), Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getHeaders().get(HttpHeaders.SET_COOKIE));
        assertEquals("Bearer", response.getBody().get("tokenType"));
        return (String) response.getBody().get("token");
    }

    private String signed(String issuer, String audience, String subject, Instant expiration) {
        return Jwts.builder().issuer(issuer).audience().add(audience).and().subject(subject)
                .issuedAt(Date.from(Instant.now().minusSeconds(60)))
                .expiration(Date.from(expiration)).signWith(KEY, Jwts.SIG.HS256).compact();
    }

    @Test
    void missingMalformedAndSessionOnlyCredentialsAreUnauthorized() {
        for (String authorization : List.of("Bearer ", "Basic abc", "Bearer not-a-jwt")) {
            assertEquals(HttpStatus.UNAUTHORIZED, getBook(authorization).getStatusCode());
        }
        ResponseEntity<Map> missing = getBook(null);
        assertEquals(HttpStatus.UNAUTHORIZED, missing.getStatusCode());
        assertEquals("Bearer", missing.getHeaders().getFirst(HttpHeaders.WWW_AUTHENTICATE));
        assertNotNull(missing.getBody().get("error"));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "JSESSIONID=old-session");
        assertEquals(HttpStatus.UNAUTHORIZED, http.exchange("/api/v1/books/1", HttpMethod.GET,
                new HttpEntity<>(headers), Map.class).getStatusCode());
    }

    @Test
    void loginThenQueryReturnsBookOnVirtualThreadWithoutSession() {
        String token = login("demo", "demo123");
        ResponseEntity<Map> response = getBook("Bearer " + token);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().get("id"));
        assertEquals("true", response.getHeaders().getFirst("X-Virtual-Thread"));
        assertNull(response.getHeaders().get(HttpHeaders.SET_COOKIE));
        assertEquals(HttpStatus.OK, getBook("bearer " + token).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, http.postForEntity("/api/v1/users/login",
                Map.of("username", "demo", "password", "wrong"), Map.class).getStatusCode());
    }

    @Test
    void tamperedExpiredWrongIssuerAudienceAndInvalidSubjectsAreRejected() {
        String token = login("demo", "demo123");
        String[] parts = token.split("\\.");
        // 改为管理员的用户 ID，但保留原签名，必须拒绝。
        String payload = new String(Base64.getUrlDecoder().decode(parts[1]), java.nio.charset.StandardCharsets.UTF_8);
        parts[1] = Base64.getUrlEncoder().withoutPadding().encodeToString(
                payload.replace("\"sub\":\"1\"", "\"sub\":\"2\"").getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertEquals(HttpStatus.UNAUTHORIZED, getBook("Bearer " + String.join(".", parts)).getStatusCode());
        for (String invalid : List.of(
                signed("bookstore", "bookstore-api", "1", Instant.now().minusSeconds(1)),
                signed("other", "bookstore-api", "1", Instant.now().plusSeconds(60)),
                signed("bookstore", "other", "1", Instant.now().plusSeconds(60)),
                signed("bookstore", "bookstore-api", "abc", Instant.now().plusSeconds(60)),
                signed("bookstore", "bookstore-api", "0", Instant.now().plusSeconds(60)),
                signed("bookstore", "bookstore-api", "999", Instant.now().plusSeconds(60)),
                Jwts.builder().subject("1").issuer("bookstore").audience().add("bookstore-api").and()
                        .issuedAt(new Date()).signWith(KEY, Jwts.SIG.HS256).compact(),
                Jwts.builder().subject("1").signWith(Jwts.SIG.HS256.key().build()).compact(),
                Jwts.builder().subject("1").compact())) {
            assertEquals(HttpStatus.UNAUTHORIZED, getBook("Bearer " + invalid).getStatusCode());
        }
    }

    @Test
    void accountDisableAndRoleChangesAreReadFromDatabase() {
        String token = login("demo", "demo123");
        var user = users.findByUserId(1L);
        try {
            user.setEnable(false);
            users.save(user);
            assertEquals(HttpStatus.UNAUTHORIZED, getBook("Bearer " + token).getStatusCode());
            user.setEnable(true);
            user.setIdentity(1);
            users.save(user);
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            assertEquals(HttpStatus.OK, http.exchange("/api/v1/users/list", HttpMethod.GET,
                    new HttpEntity<>(headers), List.class).getStatusCode());
        } finally {
            user.setEnable(true);
            user.setIdentity(0);
            users.save(user);
        }
    }

    @Test
    void authorizationAndOwnershipAreEnforcedAndMissingBookReturns404() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(login("demo", "demo123"));
        for (String url : List.of("/api/v1/users/list", "/api/v1/orders/all", "/api/v1/cart/2")) {
            assertEquals(HttpStatus.FORBIDDEN, http.exchange(url, HttpMethod.GET,
                    new HttpEntity<>(headers), Map.class).getStatusCode());
        }
        assertEquals(HttpStatus.OK, http.exchange("/api/v1/orders", HttpMethod.GET,
                new HttpEntity<>(headers), List.class).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, http.exchange("/api/v1/books/999", HttpMethod.GET,
                new HttpEntity<>(headers), String.class).getStatusCode());
    }

    @Test
    void corsPreflightAllowsAuthorizationWithoutToken() {
        HttpHeaders headers = new HttpHeaders();
        headers.setOrigin("http://localhost:5173");
        headers.setAccessControlRequestMethod(HttpMethod.GET);
        headers.setAccessControlRequestHeaders(List.of("authorization"));
        var response = http.exchange("/api/v1/books/1", HttpMethod.OPTIONS,
                new HttpEntity<>(headers), String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("http://localhost:5173", response.getHeaders().getAccessControlAllowOrigin());
        assertTrue(response.getHeaders().getAccessControlAllowHeaders().contains("authorization"));
    }

    @Test
    void concurrentHttpRequestsAllUseVirtualThreads() throws Exception {
        String token = jwt.generate(1L, 0);
        try (var executor = Executors.newFixedThreadPool(8)) {
            var tasks = IntStream.range(0, 16).mapToObj(i ->
                    executor.submit(() -> getBook("Bearer " + token))).toList();
            for (var task : tasks) {
                var response = task.get();
                assertEquals(HttpStatus.OK, response.getStatusCode());
                assertEquals("true", response.getHeaders().getFirst("X-Virtual-Thread"));
            }
        }
    }

    @Test
    void cartAndOrderUseVerifiedIdentityAndRejectOtherUsers() {
        String username = "reader-" + java.util.UUID.randomUUID();
        assertEquals(HttpStatus.OK, http.postForEntity("/api/v1/users/register", Map.of(
                "username", username, "password", "test123", "confirmPassword", "test123",
                "nickname", "Reader", "email", username + "@example.com"), Map.class).getStatusCode());
        Long userId = users.findByUsername(username).getUserId();
        HttpHeaders owner = new HttpHeaders();
        owner.setBearerAuth(login(username, "test123"));
        HttpHeaders other = new HttpHeaders();
        other.setBearerAuth(login("demo", "demo123"));
        // 客户端伪造 userId=1，加入的商品仍然必须属于 JWT 对应的新用户。
        assertEquals(HttpStatus.OK, http.exchange("/api/v1/cart", HttpMethod.POST,
                new HttpEntity<>(Map.of("userId", 1, "bookId", 1, "quantity", 2), owner), Map.class).getStatusCode());
        Map cart = http.exchange("/api/v1/cart/" + userId, HttpMethod.GET,
                new HttpEntity<>(owner), Map.class).getBody();
        Map item = (Map) ((List) cart.get("items")).get(0);
        assertEquals(userId.intValue(), item.get("userId"));
        String itemUrl = "/api/v1/cart/" + item.get("id");
        assertEquals(HttpStatus.FORBIDDEN, http.exchange(itemUrl, HttpMethod.PUT,
                new HttpEntity<>(Map.of("quantity", 3), other), Map.class).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, http.exchange(itemUrl, HttpMethod.DELETE,
                new HttpEntity<>(other), Map.class).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, http.exchange("/api/v1/cart/user/" + userId, HttpMethod.DELETE,
                new HttpEntity<>(other), Map.class).getStatusCode());
        var order = http.exchange("/api/v1/orders", HttpMethod.POST,
                new HttpEntity<>(Map.of("userId", 1, "receiver", "Reader", "address", "Test", "tel", "123"), owner), Map.class);
        assertEquals(HttpStatus.OK, order.getStatusCode());
        assertEquals(userId.intValue(), order.getBody().get("userId"));
        String orderUrl = "/api/v1/order/" + order.getBody().get("id");
        assertEquals(HttpStatus.OK, http.exchange(orderUrl, HttpMethod.GET, new HttpEntity<>(owner), Map.class).getStatusCode());
        assertEquals(HttpStatus.FORBIDDEN, http.exchange(orderUrl, HttpMethod.GET, new HttpEntity<>(other), Map.class).getStatusCode());
        assertEquals(HttpStatus.OK, http.exchange("/api/v1/cart/user/" + userId, HttpMethod.DELETE,
                new HttpEntity<>(owner), Map.class).getStatusCode());
    }

    @Test
    void adminBookCrudHasResourceStatusCodes() {
        HttpHeaders admin = new HttpHeaders();
        admin.setBearerAuth(login("admin", "admin123"));
        var created = http.exchange("/api/v1/books", HttpMethod.POST,
                new HttpEntity<>(Map.of("title", "Created Book", "author", "Test", "price", 100), admin), Map.class);
        assertEquals(HttpStatus.CREATED, created.getStatusCode());
        String path = created.getHeaders().getLocation().toString();
        assertEquals(HttpStatus.OK, http.exchange(path, HttpMethod.PATCH,
                new HttpEntity<>(Map.of("title", "Updated Book"), admin), Map.class).getStatusCode());
        assertEquals("Updated Book", http.exchange(path, HttpMethod.GET,
                new HttpEntity<>(admin), Map.class).getBody().get("title"));
        assertEquals(HttpStatus.OK, http.exchange(path, HttpMethod.DELETE, new HttpEntity<>(admin), Map.class).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, http.exchange(path, HttpMethod.DELETE, new HttpEntity<>(admin), Map.class).getStatusCode());
    }
}
