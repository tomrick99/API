CREATE TABLE IF NOT EXISTS material_mybatis (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(200) NOT NULL,
    type VARCHAR(50),
    description VARCHAR(500),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_material_mybatis_title (title),
    INDEX idx_material_mybatis_created_at (created_at)
);

CREATE TABLE IF NOT EXISTS material_folder (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    sort INT NOT NULL DEFAULT 100,
    status INT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id)
);