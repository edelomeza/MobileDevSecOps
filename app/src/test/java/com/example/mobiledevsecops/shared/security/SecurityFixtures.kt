package com.example.mobiledevsecops.shared.security

object SecurityFixtures {

    // Common weak passwords for password policy testing
    val commonPasswords = listOf(
        "password",
        "12345678",
        "qwerty123",
        "abc12345",
        "welcome1",
        "monkey123",
        "dragon123",
        "master123",
        "sunshine"
    )

    val sequentialPasswords = listOf(
        "12345678",
        "abcdefgh",
        "qwertyui",
        "87654321",
        "zyxwvuts"
    )

    val repeatedPasswords = listOf(
        "aaaaaaaa",
        "11111111",
        "********",
        "........"
    )

    // Injection payloads for input sanitization testing
    val sqlInjectionPayloads = listOf(
        "' OR '1'='1",
        "'; DROP TABLE usuarios; --",
        "\" OR 1=1 --",
        "admin'--",
        "1; SELECT * FROM usuarios",
        "' UNION SELECT * FROM usuarios --",
        "'; EXEC xp_cmdshell('dir') --"
    )

    val xssPayloads = listOf(
        "<script>alert('xss')</script>",
        "<img src=x onerror=alert(1)>",
        "javascript:alert(1)",
        "\"><script>alert(1)</script>",
        "{{constructor.constructor('alert(1)')()}}",
        "<svg onload=alert(1)>",
        "';alert(1);//"
    )

    val noSqlInjectionPayloads = listOf(
        "{\"\$gt\": \"\"}",
        "{\"\$ne\": \"\"}",
        "{\"\$where\": \"1==1\"}",
        "admin{\"\$ne\": \"\"}",
        "{\"\$regex\": \".*\"}"
    )

    val pathTraversalPayloads = listOf(
        "../../etc/passwd",
        "..\\\\..\\\\windows\\system32",
        "%2e%2e%2f%2e%2e%2f",
        "....//....//....//etc/passwd",
        "../../../etc/shadow"
    )

    // Valid credentials for testing
    val validPassword = "Str0ng!Pass#2024"
    val validEmail = "usuario@example.com"
    val validName = "Juan Pérez"

    // JWT tokens for testing
    val validJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjk5OTk5OTk5OTl9.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c"

    val expiredJwtToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyLCJleHAiOjB9.7nRjP0vM6iI6ZP0M0IX0X0X0X0X0X0X0X0X0X0X0X0"
}
