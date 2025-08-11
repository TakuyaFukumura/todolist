package com.example.myapplication.config

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import spock.lang.Specification
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder

/**
 * SecurityConfig統合テスト
 * Spring Security設定の動作確認
 */
@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
class SecurityConfigSpec extends Specification {

    @Autowired
    UserDetailsService userDetailsService

    @Autowired
    PasswordEncoder passwordEncoder

    def "ユーザー詳細サービスが設定されていること"() {
        expect: "UserDetailsServiceが利用できる"
        userDetailsService != null
    }

    def "設定されたユーザーがロードできること"() {
        when: "テストユーザーをロード"
        UserDetails user = userDetailsService.loadUserByUsername("user")

        then: "ユーザー情報が正しく設定されている"
        user.username == "user"
        user.authorities.size() == 1
        user.authorities.first().authority == "ROLE_USER"
        user.isEnabled()
        user.isAccountNonExpired()
        user.isCredentialsNonExpired()
        user.isAccountNonLocked()
    }

    def "パスワードエンコーダーが設定されていること"() {
        given: "平文パスワード"
        String rawPassword = "password"

        when: "パスワードをエンコード"
        String encodedPassword = passwordEncoder.encode(rawPassword)

        then: "エンコードされたパスワードが元のパスワードとマッチする"
        passwordEncoder.matches(rawPassword, encodedPassword)
        and: "エンコードされたパスワードは平文パスワードと異なる"
        encodedPassword != rawPassword
    }
}
