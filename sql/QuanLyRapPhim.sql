-- Script tạo database SQLite cho bài Quản lý rạp phim.
-- File database/QuanLyRapPhim.sqlite đã được tạo sẵn từ script này.
-- Muốn làm lại từ đầu: xóa file .sqlite, mở DB Browser for SQLite, tạo file mới và chạy script này.

CREATE TABLE login (
    username TEXT PRIMARY KEY,
    password TEXT NOT NULL,
    admin    INTEGER NOT NULL DEFAULT -1 -- 2 admin chính, 1 admin phụ, 0 nhân viên, -1 chưa cấp quyền
);

CREATE TABLE TablePhim (
    MaPhim        TEXT PRIMARY KEY,
    TenPhim       TEXT NOT NULL,
    TheLoai       TEXT,
    ThoiLuong     INTEGER,
    DaoDien       TEXT,
    NgayKhoiChieu TEXT
);

CREATE TABLE TableSuatChieu (
    MaSuat     TEXT PRIMARY KEY,
    MaPhim     TEXT NOT NULL,
    PhongChieu TEXT NOT NULL,
    NgayChieu  TEXT NOT NULL,
    GioChieu   TEXT NOT NULL,
    GiaVe      INTEGER NOT NULL
);

CREATE TABLE TableVe (
    MaVe        TEXT PRIMARY KEY,
    MaSuat      TEXT NOT NULL,
    TenKhach    TEXT NOT NULL,
    SoDienThoai TEXT,
    SoGhe       TEXT NOT NULL,
    GiaVe       INTEGER NOT NULL,
    NgayBan     TEXT
);

INSERT INTO login VALUES ('admin', '123', 2);
INSERT INTO login VALUES ('nhanvien', '123', 0);

INSERT INTO TablePhim VALUES ('P01', 'Lật Mặt 7', 'Gia đình', 138, 'Lý Hải', '26/04/2024');
INSERT INTO TablePhim VALUES ('P02', 'Dune: Part Two', 'Khoa học viễn tưởng', 166, 'Denis Villeneuve', '01/03/2024');
INSERT INTO TablePhim VALUES ('P03', 'Inside Out 2', 'Hoạt hình', 96, 'Kelsey Mann', '14/06/2024');
INSERT INTO TablePhim VALUES ('P04', 'Mai', 'Tâm lý', 131, 'Trấn Thành', '10/02/2024');

INSERT INTO TableSuatChieu VALUES ('S01', 'P01', 'Phòng 1', '25/10/2026', '19:00', 90000);
INSERT INTO TableSuatChieu VALUES ('S02', 'P02', 'Phòng 2', '25/10/2026', '20:30', 100000);
INSERT INTO TableSuatChieu VALUES ('S03', 'P03', 'Phòng 1', '26/10/2026', '09:00', 70000);

INSERT INTO TableVe VALUES ('V01', 'S01', 'Nguyễn Văn An', '0901234567', 'A1', 90000, '24/10/2026');
INSERT INTO TableVe VALUES ('V02', 'S01', 'Nguyễn Văn An', '0901234567', 'A2', 90000, '24/10/2026');
INSERT INTO TableVe VALUES ('V03', 'S02', 'Trần Thị Bình', '0912345678', 'C5', 100000, '24/10/2026');
