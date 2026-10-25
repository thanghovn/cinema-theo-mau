package quanlyrapphim;

import java.sql.*;
import java.text.MessageFormat;
import javax.swing.*;

public class Phim extends JInternalFrame {

    Connection conn = null;
    ResultSet rs = null;
    PreparedStatement pst = null;

    JTextField txtMaPhim = new JTextField();
    JTextField txtTenPhim = new JTextField();
    JTextField txtTheLoai = new JTextField();
    JTextField txtThoiLuong = new JTextField();
    JTextField txtDaoDien = new JTextField();
    JTextField txtNgayKhoiChieu = new JTextField();
    JTextField txtTimKiem = new JTextField();
    JRadioButton jRadioButtonMaPhim = new JRadioButton("Mã phim");
    JRadioButton jRadioButtonTenPhim = new JRadioButton("Tên phim", true);
    JRadioButton jRadioButtonTheLoai = new JRadioButton("Thể loại");
    JComboBox<String> jComboBoxSapXep = new JComboBox<>(new String[]{"Sắp xếp theo mã", "Sắp xếp theo tên", "Sắp xếp theo thời lượng"});
    JButton btnThem = new JButton("Thêm");
    JButton btnSua = new JButton("Sửa");
    JButton btnXoa = new JButton("Xóa");
    JButton btnReset = new JButton("Làm mới");
    JButton btnIn = new JButton("In");
    JTable jTablePhim = new JTable();

    public Phim() {
        initComponents();
        conn = JavaConnect.ConnectDB();
        updateTablePhim();
        // Chỉ admin được thêm/sửa/xóa phim
        btnThem.setVisible(Login.admin >= 1);
        btnSua.setVisible(Login.admin >= 1);
        btnXoa.setVisible(Login.admin >= 1);
    }

    private void initComponents() {
        setTitle("Quản lý phim");
        setBounds(0, 0, 960, 580);
        setLayout(null);

        String[] nhan = {"Mã phim:", "Tên phim:", "Thể loại:", "Thời lượng (phút):", "Đạo diễn:", "Ngày khởi chiếu:"};
        JTextField[] o = {txtMaPhim, txtTenPhim, txtTheLoai, txtThoiLuong, txtDaoDien, txtNgayKhoiChieu};
        for (int i = 0; i < o.length; i++) {
            JLabel lb = new JLabel(nhan[i]);
            lb.setBounds(20, 20 + 38 * i, 120, 25);
            add(lb);
            o[i].setBounds(140, 20 + 38 * i, 250, 25);
            add(o[i]);
        }
        JLabel lbGoiY = new JLabel("(dd/MM/yyyy)");
        lbGoiY.setBounds(395, 210, 100, 25);
        add(lbGoiY);

        btnThem.setBounds(500, 20, 80, 30);
        btnSua.setBounds(590, 20, 80, 30);
        btnXoa.setBounds(680, 20, 80, 30);
        btnReset.setBounds(770, 20, 90, 30);
        btnIn.setBounds(870, 20, 60, 30);
        add(btnThem);
        add(btnSua);
        add(btnXoa);
        add(btnReset);
        add(btnIn);

        JLabel lbTim = new JLabel("Tìm kiếm:");
        lbTim.setBounds(500, 80, 70, 25);
        add(lbTim);
        txtTimKiem.setBounds(570, 80, 360, 25);
        add(txtTimKiem);
        ButtonGroup nhom = new ButtonGroup();
        nhom.add(jRadioButtonMaPhim);
        nhom.add(jRadioButtonTenPhim);
        nhom.add(jRadioButtonTheLoai);
        jRadioButtonMaPhim.setBounds(570, 110, 90, 25);
        jRadioButtonTenPhim.setBounds(660, 110, 90, 25);
        jRadioButtonTheLoai.setBounds(750, 110, 90, 25);
        add(jRadioButtonMaPhim);
        add(jRadioButtonTenPhim);
        add(jRadioButtonTheLoai);

        jComboBoxSapXep.setBounds(570, 150, 200, 25);
        add(jComboBoxSapXep);

        JScrollPane scroll = new JScrollPane(jTablePhim);
        scroll.setBounds(20, 260, 910, 270);
        add(scroll);

        btnThem.addActionListener(e -> btnThemActionPerformed());
        btnSua.addActionListener(e -> btnSuaActionPerformed());
        btnXoa.addActionListener(e -> btnXoaActionPerformed());
        btnReset.addActionListener(e -> btnResetActionPerformed());
        btnIn.addActionListener(e -> btnInActionPerformed());
        jComboBoxSapXep.addActionListener(e -> jComboBoxSapXepItemStateChanged());
        txtTimKiem.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtTimKiemKeyReleased();
            }
        });
        jTablePhim.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTablePhimMouseClicked();
            }
        });
    }

    private void updateTablePhim() {
        String sql = "select * from TablePhim";
        try {
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            JavaConnect.loadTable(jTablePhim, rs);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    // Kiểm tra dữ liệu nhập, trả về true nếu hợp lệ
    private boolean kiemTra() {
        if (txtMaPhim.getText().trim().isEmpty() || txtTenPhim.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(null, "Mã phim và tên phim không được để trống");
            return false;
        }
        try {
            int thoiLuong = Integer.parseInt(txtThoiLuong.getText().trim());
            if (thoiLuong <= 0) {
                JOptionPane.showMessageDialog(null, "Thời lượng phải lớn hơn 0");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Thời lượng phải là số nguyên");
            return false;
        }
        if (!JavaConnect.laNgay(txtNgayKhoiChieu.getText().trim())) {
            JOptionPane.showMessageDialog(null, "Ngày khởi chiếu phải có dạng dd/MM/yyyy");
            return false;
        }
        int nam = Integer.parseInt(txtNgayKhoiChieu.getText().trim().substring(6));
        int namNay = java.time.Year.now().getValue();
        if (nam < 1900 || nam > namNay + 5) {
            JOptionPane.showMessageDialog(null, "Năm khởi chiếu phải từ 1900 đến " + (namNay + 5));
            return false;
        }
        return true;
    }

    private void btnThemActionPerformed() {
        if (!kiemTra()) {
            return;
        }
        try {
            String sql = "Insert into TablePhim(MaPhim,TenPhim,TheLoai,ThoiLuong,DaoDien,NgayKhoiChieu) values (?,?,?,?,?,?)";
            pst = conn.prepareStatement(sql);
            pst.setString(1, txtMaPhim.getText().trim());
            pst.setString(2, txtTenPhim.getText().trim());
            pst.setString(3, txtTheLoai.getText().trim());
            pst.setInt(4, Integer.parseInt(txtThoiLuong.getText().trim()));
            pst.setString(5, txtDaoDien.getText().trim());
            pst.setString(6, txtNgayKhoiChieu.getText().trim());
            pst.execute();
            JOptionPane.showMessageDialog(null, "Lưu thành công");
        } catch (SQLException e) {
            // Trùng khóa chính MaPhim sẽ báo lỗi ở đây
            JOptionPane.showMessageDialog(null, "Không thêm được (có thể trùng mã phim):\n" + e.getMessage());
        }
        updateTablePhim();
    }

    private void btnSuaActionPerformed() {
        if (!kiemTra()) {
            return;
        }
        try {
            String sql = "update TablePhim set TenPhim=?,TheLoai=?,ThoiLuong=?,DaoDien=?,NgayKhoiChieu=? where MaPhim=?";
            pst = conn.prepareStatement(sql);
            pst.setString(1, txtTenPhim.getText().trim());
            pst.setString(2, txtTheLoai.getText().trim());
            pst.setInt(3, Integer.parseInt(txtThoiLuong.getText().trim()));
            pst.setString(4, txtDaoDien.getText().trim());
            pst.setString(5, txtNgayKhoiChieu.getText().trim());
            pst.setString(6, txtMaPhim.getText().trim());
            int n = pst.executeUpdate();
            if (n == 0) {
                JOptionPane.showMessageDialog(null, "Không tìm thấy mã phim để sửa");
            } else {
                JOptionPane.showMessageDialog(null, "Sửa thành công");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
        updateTablePhim();
    }

    private void btnXoaActionPerformed() {
        int p = JOptionPane.showConfirmDialog(null, "Bạn chắc chắn muốn xóa", "Xóa", JOptionPane.YES_NO_OPTION);
        if (p == 0) {
            try {
                // Phim đã có suất chiếu thì không cho xóa
                pst = conn.prepareStatement("select * from TableSuatChieu where MaPhim=?");
                pst.setString(1, txtMaPhim.getText().trim());
                rs = pst.executeQuery();
                if (rs.next()) {
                    JOptionPane.showMessageDialog(null, "Phim này đang có suất chiếu, hãy xóa suất chiếu trước");
                    return;
                }
                String sql = "delete from TablePhim where MaPhim =?";
                pst = conn.prepareStatement(sql);
                pst.setString(1, txtMaPhim.getText().trim());
                pst.execute();
                JOptionPane.showMessageDialog(null, "Xóa thành công");
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null, e);
            }
            updateTablePhim();
            btnResetActionPerformed();
        }
    }

    private void btnResetActionPerformed() {
        txtMaPhim.setText("");
        txtTenPhim.setText("");
        txtTheLoai.setText("");
        txtThoiLuong.setText("");
        txtDaoDien.setText("");
        txtNgayKhoiChieu.setText("");
        txtTimKiem.setText("");
        updateTablePhim();
    }

    private void btnInActionPerformed() {
        MessageFormat header = new MessageFormat("Danh sách phim");
        MessageFormat footer = new MessageFormat("Trang {0,number,integer}");
        try {
            jTablePhim.print(JTable.PrintMode.FIT_WIDTH, header, footer);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Không thể in: " + e.getMessage());
        }
    }

    // Bấm vào 1 dòng trong bảng thì đưa dữ liệu lên các ô nhập
    private void jTablePhimMouseClicked() {
        int row = jTablePhim.getSelectedRow();
        if (row < 0) {
            return;
        }
        txtMaPhim.setText(jTablePhim.getValueAt(row, 0).toString());
        txtTenPhim.setText(jTablePhim.getValueAt(row, 1).toString());
        txtTheLoai.setText(jTablePhim.getValueAt(row, 2).toString());
        txtThoiLuong.setText(jTablePhim.getValueAt(row, 3).toString());
        txtDaoDien.setText(jTablePhim.getValueAt(row, 4).toString());
        txtNgayKhoiChieu.setText(jTablePhim.getValueAt(row, 5).toString());
    }

    private void txtTimKiemKeyReleased() {
        String sql = "select * from TablePhim where TenPhim like ?";
        if (jRadioButtonMaPhim.isSelected()) {
            sql = "select * from TablePhim where MaPhim like ?";
        }
        if (jRadioButtonTheLoai.isSelected()) {
            sql = "select * from TablePhim where TheLoai like ?";
        }
        try {
            pst = conn.prepareStatement(sql);
            pst.setString(1, "%" + txtTimKiem.getText().trim() + "%");
            rs = pst.executeQuery();
            JavaConnect.loadTable(jTablePhim, rs);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }

    private void jComboBoxSapXepItemStateChanged() {
        String sql = "select * from TablePhim order by MaPhim";
        int index = jComboBoxSapXep.getSelectedIndex();
        if (index == 1) {
            sql = "select * from TablePhim order by TenPhim";
        }
        if (index == 2) {
            sql = "select * from TablePhim order by ThoiLuong";
        }
        try {
            pst = conn.prepareStatement(sql);
            rs = pst.executeQuery();
            JavaConnect.loadTable(jTablePhim, rs);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e);
        }
    }
}
