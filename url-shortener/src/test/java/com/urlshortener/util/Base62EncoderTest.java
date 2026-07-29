package com.urlshortener.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class Base62EncoderTest {

    private Base62Encoder encoder;

    @BeforeEach
    void setUp() {
        encoder = new Base62Encoder();  // no mocks needed — pure utility
    }

    @Test
    @DisplayName("Should generate code of default length 6")
    void shouldGenerateCode_ofDefaultLength() {
        // Arrange — nothing needed

        // Act
        String code = encoder.generate();

        // Assert
        assertThat(code).isNotNull();
        assertThat(code).hasSize(6);
    }

    @Test
    @DisplayName("Should generate code with only Base62 characters")
    void shouldGenerateCode_withOnlyBase62Characters() {
        String code = encoder.generate();

        assertThat(code).matches("[a-zA-Z0-9]+");
    }

    @Test
    @DisplayName("Should generate code of custom length")
    void shouldGenerateCode_ofCustomLength() {
        String code = encoder.generate(8);

        assertThat(code).hasSize(8);
    }

    @Test
    @DisplayName("Should generate unique codes on repeated calls")
    void shouldGenerateUniqueCodes_onRepeatedCalls() {
        String code1 = encoder.generate();
        String code2 = encoder.generate();
        String code3 = encoder.generate();

        // statistically guaranteed — 56 billion combinations
        assertThat(code1).isNotEqualTo(code2);
        assertThat(code2).isNotEqualTo(code3);
    }
}