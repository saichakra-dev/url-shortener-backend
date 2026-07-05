package com.urlshortener.domain.entity;

import com.urlshortener.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "users")  // MongoDB — NOT @Entity
@Data                            // generates getters, setters, equals, hashCode, toString
@Builder                         // generates builder pattern
@NoArgsConstructor               // required by MongoDB for deserialization
@AllArgsConstructor              // required by @Builder when used together
public class User {

    @Id
    private String id;           // String, not int — MongoDB ObjectId maps to String

    private String name;

    @Indexed(unique = true)      // creates unique index in MongoDB
    private String email;

    private String password;     // ALWAYS stored as BCrypt hash, never plaintext

    private Role role;

    @Builder.Default             // tells Lombok builder to use this default value
    private Instant createdAt = Instant.now();

    @Builder.Default
    private boolean enabled = true;
}