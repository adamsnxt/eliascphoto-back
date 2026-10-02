CREATE TABLE reviews (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    review_text VARCHAR(3000) NOT NULL,
    rate REAL NOT NULL CHECK (rate >= 1 AND rate <= 5),
    is_active BOOLEAN NOT NULL DEFAULT FALSE
);