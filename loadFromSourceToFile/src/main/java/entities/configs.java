package entities;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "configs", schema = "db_controller", catalog = "")
public class configs {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "file_name", nullable = true, length = 255)
    private String fileName;
    @Basic
    @Column(name = "source_path", nullable = true, length = 255)
    private String sourcePath;
    @Basic
    @Column(name = "file_location", nullable = true, length = 255)
    private String fileLocation;
    @Basic
    @Column(name = "backup_path", nullable = true, length = 255)
    private String backupPath;
    @Basic
    @Column(name = "warehouse_procedure", nullable = true, length = 100)
    private String warehouseProcedure;
    @Basic
    @Column(name = "version", nullable = true, length = 50)
    private String version;
    @Basic
    @Column(name = "is_active", nullable = true)
    private Byte isActive;
    @Basic
    @Column(name = "insert_date", nullable = false)
    private Timestamp insertDate;
    @Basic
    @Column(name = "update_date", nullable = false)
    private Timestamp updateDate;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getFileLocation() {
        return fileLocation;
    }

    public void setFileLocation(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }

    public String getWarehouseProcedure() {
        return warehouseProcedure;
    }

    public void setWarehouseProcedure(String warehouseProcedure) {
        this.warehouseProcedure = warehouseProcedure;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Byte getIsActive() {
        return isActive;
    }

    public void setIsActive(Byte isActive) {
        this.isActive = isActive;
    }

    public Timestamp getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(Timestamp insertDate) {
        this.insertDate = insertDate;
    }

    public Timestamp getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Timestamp updateDate) {
        this.updateDate = updateDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        configs that = (configs) o;

        if (id != that.id) return false;
        if (fileName != null ? !fileName.equals(that.fileName) : that.fileName != null) return false;
        if (sourcePath != null ? !sourcePath.equals(that.sourcePath) : that.sourcePath != null) return false;
        if (fileLocation != null ? !fileLocation.equals(that.fileLocation) : that.fileLocation != null) return false;
        if (backupPath != null ? !backupPath.equals(that.backupPath) : that.backupPath != null) return false;
        if (warehouseProcedure != null ? !warehouseProcedure.equals(that.warehouseProcedure) : that.warehouseProcedure != null)
            return false;
        if (version != null ? !version.equals(that.version) : that.version != null) return false;
        if (isActive != null ? !isActive.equals(that.isActive) : that.isActive != null) return false;
        if (insertDate != null ? !insertDate.equals(that.insertDate) : that.insertDate != null) return false;
        if (updateDate != null ? !updateDate.equals(that.updateDate) : that.updateDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (fileName != null ? fileName.hashCode() : 0);
        result = 31 * result + (sourcePath != null ? sourcePath.hashCode() : 0);
        result = 31 * result + (fileLocation != null ? fileLocation.hashCode() : 0);
        result = 31 * result + (backupPath != null ? backupPath.hashCode() : 0);
        result = 31 * result + (warehouseProcedure != null ? warehouseProcedure.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (isActive != null ? isActive.hashCode() : 0);
        result = 31 * result + (insertDate != null ? insertDate.hashCode() : 0);
        result = 31 * result + (updateDate != null ? updateDate.hashCode() : 0);
        return result;
    }
}
