# Ôn tập bảo vệ: Quản lý rạp phim (bản theo mẫu Project_Java)

Mục tiêu: khi thầy bảo "sửa chỗ này", "thêm chức năng kia", bạn biết **mở file nào, tìm hàm nào, sửa ra sao, kiểm tra thế nào**,
không cần AI. Bạn không cần thuộc code. Chỉ cần nắm bản đồ (mục 1), luồng chạy (mục 2) và một công thức (mục 3).

---

## 1. Bản đồ: muốn sửa gì thì mở file nào

Cả project chỉ có 1 package `quanlyrapphim`, 7 file. Mỗi màn hình là 1 file.

| Thầy yêu cầu | Mở file | Tìm (Ctrl+F) |
|---|---|---|
| Đổi đường dẫn database, đổi cách đổ dữ liệu vào bảng | `JavaConnect.java` | `ConnectDB`, `loadTable` |
| Đổi cách kiểm tra ngày | `JavaConnect.java` | `laNgay` |
| Sửa đăng nhập | `Login.java` | `btn_loginActionPerformed` |
| Thêm/bớt mục menu, đổi đồng hồ, đăng xuất | `MainActivity.java` | `JMenuItem`, `Timer`, `hienThi` |
| Thêm/sửa/xóa phim, điều kiện nhập phim | `Phim.java` | `btnThemActionPerformed`, `kiemTra` |
| Tìm kiếm / sắp xếp phim | `Phim.java` | `txtTimKiemKeyReleased`, `jComboBoxSapXep` |
| Luật trùng lịch chiếu | `SuatChieu.java` | `trungLich` |
| Điều kiện giờ chiếu, giá vé | `SuatChieu.java` | `laGio`, `kiemTra` |
| Luật trùng ghế, số điện thoại | `BanVe.java` | `gheDaBan`, `kiemTra` |
| Doanh thu | `BanVe.java` | `tinhDoanhThu` |
| Thông tin suất hiện ra khi chọn mã suất | `BanVe.java` | `hienThongTinSuat` |
| Đổi mật khẩu, thêm tài khoản | `TaiKhoan.java` | `btnTaiKhoanLuuActionPerformed`, `btnThemTaiKhoanActionPerformed` |
| Vị trí, kích thước ô nhập / nút | file của màn hình đó | `setBounds` trong `initComponents` |
| Dữ liệu mẫu, cấu trúc bảng | `sql/QuanLyRapPhim.sql` | `CREATE TABLE` |

**Mọi màn hình (Phim, SuatChieu, BanVe) có cùng bộ hàm**, nên học kỹ 1 file `Phim.java` là hiểu cả 3:

| Hàm | Làm gì |
|---|---|
| `initComponents()` | Tạo nhãn, ô nhập, nút, bảng; đặt vị trí bằng `setBounds(x, y, rộng, cao)`; gắn sự kiện cho nút |
| `updateTableXxx()` | `select *` rồi đổ vào bảng |
| `kiemTra()` | Kiểm tra dữ liệu nhập; sai thì báo và trả về `false` |
| `btnThemActionPerformed()` | `kiemTra` → `INSERT` → tải lại bảng |
| `btnSuaActionPerformed()` | `kiemTra` → `UPDATE ... where Ma=?` → tải lại bảng |
| `btnXoaActionPerformed()` | Hỏi chắc chưa → kiểm tra còn dữ liệu liên quan không → `DELETE` |
| `btnResetActionPerformed()` | Xóa trắng các ô, tải lại bảng |
| `btnInActionPerformed()` | `jTable.print(...)` in bảng |
| `jTableXxxMouseClicked()` | Bấm 1 dòng → đưa dữ liệu dòng đó lên các ô |
| `txtTimKiemKeyReleased()` | Gõ phím vào ô tìm → `select ... like ?` |

### 3 phím NetBeans cứu bạn
- **Ctrl + Click** vào tên hàm: nhảy tới chỗ hàm được viết.
- **Alt + F7** (Find Usages): xem hàm/biến được dùng ở đâu.
- **Ctrl + F**: tìm chữ trong file. Mẹo: tìm đúng **chữ hiện trên giao diện** (ví dụ `"Bán vé"`) là ra ngay đoạn code của nút đó.

---

## 2. Luồng chạy của từng chức năng (phải nói được)

### 2.1 Mở chương trình và đăng nhập

```
Login.main()
  -> new Login(): initComponents() vẽ form, JavaConnect.ConnectDB() mở kết nối SQLite
Bấm "Đăng nhập" (hoặc Enter ở ô mật khẩu) -> btn_loginActionPerformed()
  1. select * from login where username=? and password=?
  2. rs.next() có dòng  -> đúng: lưu Login.useNameLogin, Login.admin (biến static)
                           mở new MainActivity(), dispose() cửa sổ đăng nhập
     rs.next() không có -> báo "Tài khoản hoặc mật khẩu không đúng"
```

### 2.2 Màn hình chính

```
new MainActivity()
  -> initComponents(): tạo thanh menu "Chức năng" và JDesktopPane
  -> displayAllTable(): tạo sẵn 4 cửa sổ con Phim, SuatChieu, BanVe, TaiKhoan, thêm vào JDesktopPane
  -> Timer 1 giây: cập nhật nhãn giờ
Bấm menu "Suất chiếu" -> suatChieu.fillComboMaPhim() (lấy lại danh sách mã phim, vì có thể vừa thêm phim)
                      -> hienThi(suatChieu): ẩn 3 cửa sổ kia, hiện cửa sổ này
```

### 2.3 Thêm phim (Sửa, Xóa đi đường tương tự)

```
Bấm "Thêm" -> btnThemActionPerformed()
  1. kiemTra(): mã/tên trống? thời lượng có phải số > 0? ngày có đúng dd/MM/yyyy?
     sai -> JOptionPane báo lỗi, return (không lưu)
  2. INSERT INTO TablePhim(...) values (?,?,?,?,?,?)  + pst.setString/setInt từng dấu ?
  3. trùng mã phim -> SQLite ném SQLException (vì MaPhim là PRIMARY KEY) -> catch báo lỗi
  4. updateTablePhim() -> bảng tải lại
```

### 2.4 Xóa phim

```
btnXoaActionPerformed()
  1. showConfirmDialog: chọn Yes thì p == 0
  2. select * from TableSuatChieu where MaPhim=?  -> có dòng thì không cho xóa (return)
  3. delete from TablePhim where MaPhim=?
```
SuatChieu xóa cũng y hệt: kiểm tra bảng `TableVe` trước.

### 2.5 Thêm suất chiếu (có luật trùng lịch)

```
btnThemActionPerformed() trong SuatChieu
  1. kiemTra(): mã suất, mã phim, ngày (laNgay), giờ (laGio: đúng HH:mm, giờ<24, phút<60), giá vé > 0
  2. trungLich(): select * where PhongChieu=? and NgayChieu=? and GioChieu=? and MaSuat<>?
     có dòng -> báo "Phòng này đã có suất chiếu vào giờ đó"
     (MaSuat<>? để khi SỬA thì không tự đụng với chính mình)
  3. INSERT
```

### 2.6 Bán vé (chức năng nghiệp vụ chính)

```
Chọn mã suất trong combo -> hienThongTinSuat()
  select s.*, p.TenPhim from TableSuatChieu s join TablePhim p on s.MaPhim = p.MaPhim where s.MaSuat=?
  -> hiện "Phim: ... | Phòng ... | giờ ngày", tự điền giá vé (ô giá không cho sửa)
Bấm "Bán vé" -> btnThemActionPerformed()
  1. kiemTra(): mã vé, tên khách, số ghế không trống; SĐT nếu có thì đúng 10 số bắt đầu bằng 0 (regex 0\d{9})
  2. gheDaBan(): select * from TableVe where MaSuat=? and SoGhe=? and MaVe<>?  -> có thì báo trùng ghế
  3. INSERT, NgayBan = ngày hôm nay (SimpleDateFormat)
  4. updateTableVe() -> tinhDoanhThu(): select count(*), sum(GiaVe) from TableVe
```

### 2.7 Đổi mật khẩu

```
btnTaiKhoanLuuActionPerformed()
  mật khẩu mới trống / nhập lại không khớp -> báo lỗi
  update login set password=? where username=? and password=?(cũ)
  executeUpdate() trả về 0 dòng -> nghĩa là mật khẩu cũ sai
```
Phần "Thêm tài khoản", "Cấp / tước quyền" chỉ được vẽ ra khi `Login.admin >= 1` (xem `if` trong `initComponents` của `TaiKhoan`).

---

## 3. Công thức quan trọng nhất: thêm 1 cột mới

Ví dụ thầy bảo: **"Thêm cột Quốc gia cho phim."** Làm đúng 6 bước, tất cả trong `Phim.java` trừ bước 1.

**① Database**: mở `database/QuanLyRapPhim.sqlite` bằng DB Browser for SQLite → tab Execute SQL:
```sql
ALTER TABLE TablePhim ADD COLUMN QuocGia TEXT DEFAULT '';
```
Bấm **Write Changes** (Ctrl+S), rồi đóng DB Browser (nếu để mở, Java có thể báo "database is locked").

**② Khai báo ô nhập** (cạnh các `JTextField` khác):
```java
JTextField txtQuocGia = new JTextField();
```

**③ Thêm vào giao diện**: trong `initComponents`, thêm vào cuối 2 mảng:
```java
String[] nhan = {..., "Ngày khởi chiếu:", "Quốc gia:"};
JTextField[] o = {..., txtNgayKhoiChieu, txtQuocGia};
```
Vòng `for` tự xếp ô mới xuống dưới. Vì form dài thêm 38px, dời bảng xuống: `scroll.setBounds(20, 300, 910, 230);`

**④ INSERT** trong `btnThemActionPerformed`: thêm `QuocGia` vào danh sách cột, thêm 1 dấu `?`, thêm
```java
pst.setString(7, txtQuocGia.getText().trim());
```

**⑤ UPDATE** trong `btnSuaActionPerformed`: thêm `,QuocGia=?` **trước** `where`, rồi **đánh lại số**:
QuocGia là số 6, `MaPhim` (trong where) thành số 7.

**⑥ Bấm bảng và Làm mới**: trong `jTablePhimMouseClicked` thêm
`txtQuocGia.setText(jTablePhim.getValueAt(row, 6).toString());` (cột thứ 7 → chỉ số 6, vì đếm từ 0);
trong `btnResetActionPerformed` thêm `txtQuocGia.setText("");`

**Kiểm tra:** Run → thêm 1 phim có quốc gia → thấy trong bảng → bấm dòng đó thấy hiện lên ô → tắt app mở lại vẫn còn.

Lỗi hay gặp: quên đánh lại số `?` ở UPDATE → báo lỗi hoặc sửa nhầm dữ liệu. Đếm dấu `?` trong câu SQL, phải bằng số lệnh `pst.setXxx`.

---

## 4. Bài luyện (tự làm, mỗi bài 10–30 phút, đáp án ở mục 7)

1. **Thời lượng phim tối đa 300 phút.** Nhập 400 phải báo lỗi.
2. **Giá vé tối thiểu 30000 đồng.**
3. **Thêm lựa chọn sắp xếp phim "theo ngày khởi chiếu".**
4. **Tìm phim theo đạo diễn** (thêm 1 nút radio).
5. **Nhân viên không được xóa phim**, chỉ admin được xóa.
6. **Số ghế chỉ được từ A1 đến J12.** Nhập K5 hoặc A15 phải báo lỗi.
7. **Bảng suất chiếu hiện thêm cột Tên phim** (dùng JOIN).
8. **Không cho thêm suất chiếu vào ngày đã qua.**
9. **Đồng hồ chỉ hiện giờ:phút**, bỏ ngày và giây.
10. **Thêm cột "Quốc gia" cho phim** theo mục 3 (bài tổng hợp, quan trọng nhất).

---

## 5. Câu hỏi thầy hay hỏi + trả lời ngắn

**Kiến trúc chương trình?**
Swing. `Login` là `JFrame` đăng nhập. `MainActivity` là `JFrame` chính chứa `JDesktopPane`. Mỗi chức năng là một
`JInternalFrame` (cửa sổ con nằm trong cửa sổ chính). Bấm menu thì hàm `hienThi` ẩn các cửa sổ khác, hiện cửa sổ được chọn.
Kết nối database gom vào `JavaConnect`.

**Các bước làm việc với JDBC?**
1) Nạp driver `Class.forName("org.sqlite.JDBC")`; 2) mở kết nối `DriverManager.getConnection("jdbc:sqlite:...")`;
3) tạo `PreparedStatement` với câu SQL có dấu `?`; 4) `setString`/`setInt` cho từng dấu `?`;
5) chạy: `executeQuery()` cho SELECT (trả về `ResultSet`), `executeUpdate()` cho INSERT/UPDATE/DELETE (trả về số dòng bị ảnh hưởng);
6) đọc `ResultSet` bằng `while (rs.next())`.

**`PreparedStatement` khác `Statement` thế nào? Tại sao dùng dấu `?`**
Dấu `?` để dữ liệu người dùng nhập được gửi riêng, không dính vào câu SQL, nên chống được **SQL Injection**.
Ví dụ nếu cộng chuỗi, nhập mật khẩu `' or '1'='1` có thể đăng nhập không cần mật khẩu. Bài mẫu gốc cộng chuỗi ở câu UPDATE; bài này đã sửa hết sang `?`.

**Tại sao cả chương trình dùng chung 1 kết nối?**
SQLite là 1 file. Lúc đầu mỗi màn hình mở 1 kết nối riêng (giống bài mẫu) thì khi chạy thử bị lỗi
`database is locked`: một kết nối đang đọc dở thì kết nối khác không ghi được. Nên `ConnectDB()` giữ kết nối trong biến
`static conn`, lần sau gọi thì trả lại đúng kết nối đó.

**Sao biết trùng mã phim?**
`MaPhim` là `PRIMARY KEY`, thêm trùng thì SQLite ném `SQLException`, khối `catch` báo "có thể trùng mã phim".

**Bảng có khóa ngoại không? Sao xóa phim lại bị chặn?**
Về thiết kế, `TableSuatChieu.MaPhim` tham chiếu `TablePhim.MaPhim`, `TableVe.MaSuat` tham chiếu `TableSuatChieu.MaSuat`.
Nhưng SQLite mặc định **tắt** kiểm tra khóa ngoại, nên chương trình tự kiểm tra bằng code: trước khi xóa phim thì `select`
xem phim còn suất chiếu không; còn thì không cho xóa.

**Kiểm tra trùng lịch thế nào? Tại sao có `MaSuat<>?`**
Tìm suất khác có cùng phòng, cùng ngày, cùng giờ. `MaSuat<>?` loại chính suất đang sửa ra, nếu không thì bấm Sửa mà
không đổi giờ cũng bị báo trùng. Hạn chế: chỉ so đúng giờ bắt đầu, chưa tính thời lượng phim (19:00 và 19:30 cùng phòng vẫn cho qua).
Muốn chặt hơn phải lấy thời lượng phim để tính giờ kết thúc.

**`kiemTra()` trả về boolean để làm gì?**
Để nút Thêm và Sửa dùng chung. Trong nút: `if (!kiemTra()) return;` nghĩa là dữ liệu sai thì dừng, không chạy SQL.

**`laNgay` kiểm tra ngày thế nào?**
`SimpleDateFormat("dd/MM/yyyy")` với `setLenient(false)`: không "chiều" nên ngày 31/02 bị coi là sai (nếu để lenient
thì nó tự đổi thành 03/03). Kiểm tra thêm độ dài 10 ký tự để chặn kiểu `1/1/2024`.

**Sao sắp xếp theo ngày phải dùng `substr`?**
Ngày lưu dạng chữ `dd/MM/yyyy`, sắp xếp chữ sẽ so ngày trước, sai. `substr` cắt ra năm, tháng, ngày rồi sắp theo thứ tự đó.
Cách tốt hơn là lưu ngày dạng `yyyy-MM-dd`.

**`loadTable` làm gì? Sao không dùng `DbUtils` như bài mẫu?**
Nó lấy tên cột từ `rs.getMetaData()`, tạo `DefaultTableModel`, `while (rs.next())` thêm từng dòng, rồi `table.setModel(model)`.
Tự viết để không phụ thuộc thư viện `rs2xml.jar` và để giải thích được. `isCellEditable` trả `false` để người dùng không sửa thẳng trên bảng.

**Bấm vào bảng thì sao dữ liệu hiện lên ô?**
`jTablePhimMouseClicked`: `getSelectedRow()` lấy số dòng, `getValueAt(row, cot)` lấy từng ô, `setText` vào ô nhập. Cột đếm từ 0.

**Sự kiện nút được gắn thế nào?**
`btnThem.addActionListener(e -> btnThemActionPerformed());` Biểu thức `e -> ...` là lambda: khi nút được bấm thì gọi hàm đó.

**Đồng hồ chạy thế nào? Sao không dùng Thread như bài mẫu?**
`javax.swing.Timer(1000, ...)` gọi code mỗi 1000 ms, chạy trên luồng giao diện nên an toàn. Bài mẫu dùng `while(true)` không nghỉ,
làm CPU chạy liên tục.

**Phân quyền admin làm thế nào?**
Cột `admin` trong bảng `login`: 2 = admin chính, 1 = admin phụ, 0 = nhân viên, -1 = chưa cấp quyền (vào màn chờ `ChoCapQuyen`).
Câu `update ... where admin<>2` đảm bảo không ai tước được quyền admin chính. Lúc đăng nhập lưu vào biến `static int admin` của `Login`, màn hình khác đọc `Login.admin`.
`static` nghĩa là biến thuộc về lớp, không cần tạo đối tượng vẫn dùng được.

**Thể hiện OOP ở đâu?**
Kế thừa: `Phim extends JInternalFrame`, `Login extends JFrame`, được sẵn hết hành vi cửa sổ. Ghi đè: `isCellEditable` trong `loadTable`.
Đóng gói: mỗi màn hình tự quản ô nhập và hàm của nó. Hạn chế thật: chưa tách lớp dữ liệu (model) và lớp SQL (DAO); SQL nằm luôn trong file giao diện, giống bài mẫu.

**Giao diện làm bằng kéo thả hay viết tay?**
Viết tay, `setLayout(null)` và `setBounds(x, y, rộng, cao)` cho từng thành phần. Dễ hiểu, dễ dời vị trí, nhưng cửa sổ phóng to thì
các thành phần không tự giãn. Bài mẫu dùng NetBeans GUI Builder (file `.form`).

**Mật khẩu lưu thế nào? Có an toàn không?**
Lưu chữ thường trong database, chưa an toàn (giống bài mẫu). Cải tiến: lưu mã băm (ví dụ SHA-256 kèm salt, hoặc BCrypt) thay vì mật khẩu thật.

**`executeUpdate()` trả về 0 nghĩa là gì?**
Không có dòng nào bị ảnh hưởng. Ở Sửa: mã không tồn tại. Ở đổi mật khẩu: mật khẩu cũ sai.

---

## 6. Kế hoạch ôn 3 ngày

- **Ngày 1:** chạy project, bấm thử mọi nút. Đọc `JavaConnect`, `Login`, `MainActivity`. Tự nói lại luồng 2.1, 2.2.
- **Ngày 2:** đọc kỹ `Phim.java` theo bảng hàm ở mục 1, rồi lướt `SuatChieu`, `BanVe` (để thấy chúng giống nhau). Làm bài 1–5.
- **Ngày 3:** làm bài 6–10, nhất là bài 10. Che phần trả lời ở mục 5, tự trả lời thành tiếng.

Trước khi tập sửa, **copy cả thư mục project ra một bản dự phòng** (và cả file `.sqlite`), sửa hỏng thì chép lại.

---

## 7. Gợi ý đáp án (chỉ xem sau khi đã thử)

1. `Phim.kiemTra()`: đổi `if (thoiLuong <= 0)` thành `if (thoiLuong <= 0 || thoiLuong > 300)` và sửa câu báo lỗi.
2. `SuatChieu.kiemTra()`: đổi `<= 0` của giá vé thành `< 30000`, sửa câu báo lỗi.
3. `Phim.java`: thêm `"Sắp xếp theo ngày khởi chiếu"` vào cuối mảng của `jComboBoxSapXep` (thành chỉ số 3). Trong `jComboBoxSapXepItemStateChanged` thêm:
   ```java
   if (index == 3) {
       sql = "select * from TablePhim order by substr(NgayKhoiChieu,7,4), substr(NgayKhoiChieu,4,2), substr(NgayKhoiChieu,1,2)";
   }
   ```
4. `Phim.java`: khai báo `JRadioButton jRadioButtonDaoDien = new JRadioButton("Đạo diễn");`, trong `initComponents` thêm `nhom.add(...)`,
   `setBounds(840, 110, 90, 25)`, `add(...)`. Trong `txtTimKiemKeyReleased` thêm
   `if (jRadioButtonDaoDien.isSelected()) { sql = "select * from TablePhim where DaoDien like ?"; }`
5. Cách 1, đầu `Phim.btnXoaActionPerformed()`: `if (Login.admin != 1) { JOptionPane.showMessageDialog(null, "Chỉ admin được xóa phim"); return; }`.
   Cách 2, trong constructor `Phim()`: `btnXoa.setEnabled(Login.admin == 1);` (nút bị mờ).
6. `BanVe.kiemTra()`, trước `return true;`:
   ```java
   if (!txtSoGhe.getText().trim().toUpperCase().matches("[A-J]([1-9]|1[0-2])")) {
       JOptionPane.showMessageDialog(null, "Số ghế phải từ A1 đến J12");
       return false;
   }
   ```
   Giải thích regex: `[A-J]` một chữ từ A đến J; `([1-9]|1[0-2])` là 1–9 hoặc 10–12.
7. `SuatChieu.updateTableSuatChieu()`: đổi SQL thành
   `select s.*, p.TenPhim from TableSuatChieu s join TablePhim p on s.MaPhim = p.MaPhim`.
   Cột TenPhim nằm cuối nên `jTableSuatChieuMouseClicked` không phải đổi số cột. Muốn tìm kiếm/sắp xếp cũng hiện tên phim thì sửa các câu SQL ở đó tương tự.
8. `SuatChieu.kiemTra()`, sau khi `laNgay` đã đúng, thêm (nhớ `import java.text.SimpleDateFormat;` và `import java.util.Date;`):
   ```java
   try {
       SimpleDateFormat f = new SimpleDateFormat("dd/MM/yyyy");
       Date ngay = f.parse(txtNgayChieu.getText().trim());
       Date homNay = f.parse(f.format(new Date())); // bỏ phần giờ, chỉ giữ ngày
       if (ngay.before(homNay)) {
           JOptionPane.showMessageDialog(null, "Không được thêm suất chiếu vào ngày đã qua");
           return false;
       }
   } catch (java.text.ParseException e) {
       return false;
   }
   ```
   Lưu ý: hàm này dùng cả cho nút Sửa, nên suất cũ đã qua cũng không sửa được. Nếu thầy chỉ muốn chặn khi Thêm thì đặt đoạn này vào `btnThemActionPerformed`.
9. `MainActivity`: trong `Timer`, đổi `"dd/MM/yyyy HH:mm:ss"` thành `"HH:mm"`.
10. Xem mục 3.
