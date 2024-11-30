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
    @Column(name = "source_path", nullable = true, length = 255)
    private String sourcePath;
    @Basic
    @Column(name = "backup_path", nullable = true, length = 255)
    private String backupPath;
    @Basic
    @Column(name = "staging_config", nullable = true)
    private Integer stagingConfig;
    @Basic
    @Column(name = "datawarehouse_config", nullable = true)
    private Integer datawarehouseConfig;
    @Basic
    @Column(name = "staging_table", nullable = true, length = 50)
    private String stagingTable;
    @Basic
    @Column(name = "datawarehouse_table", nullable = true, length = 50)
    private String datawarehouseTable;
    @Basic
    @Column(name = "period", nullable = true, length = -1)
    private String period;
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

    public String getSourcePath() {
        return sourcePath;
    }

    public void setSourcePath(String sourcePath) {
        this.sourcePath = sourcePath;
    }

    public String getBackupPath() {
        return backupPath;
    }

    public void setBackupPath(String backupPath) {
        this.backupPath = backupPath;
    }

    public Integer getStagingConfig() {
        return stagingConfig;
    }

    public void setStagingConfig(Integer stagingConfig) {
        this.stagingConfig = stagingConfig;
    }

    public Integer getDatawarehouseConfig() {
        return datawarehouseConfig;
    }

    public void setDatawarehouseConfig(Integer datawarehouseConfig) {
        this.datawarehouseConfig = datawarehouseConfig;
    }

    public String getStagingTable() {
        return stagingTable;
    }

    public void setStagingTable(String stagingTable) {
        this.stagingTable = stagingTable;
    }

    public String getDatawarehouseTable() {
        return datawarehouseTable;
    }

    public void setDatawarehouseTable(String datawarehouseTable) {
        this.datawarehouseTable = datawarehouseTable;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
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
    public String toString() {
        return "configs{" +
                "id=" + id +
                ", sourcePath='" + sourcePath + '\'' +
                ", backupPath='" + backupPath + '\'' +
                ", stagingConfig=" + stagingConfig +
                ", datawarehouseConfig=" + datawarehouseConfig +
                ", stagingTable='" + stagingTable + '\'' +
                ", datawarehouseTable='" + datawarehouseTable + '\'' +
                ", period='" + period + '\'' +
                ", version='" + version + '\'' +
                ", isActive=" + isActive +
                ", insertDate=" + insertDate +
                ", updateDate=" + updateDate +
                '}';
    }
}
