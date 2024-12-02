package entities;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "logs", schema = "db_controller", catalog = "")
public class logs {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "config_id", nullable = true)
    private Integer configId;
    @Basic
    @Column(name = "status", nullable = true, length = 100)
    private String status;
    @Basic
    @Column(name = "message", nullable = true, length = -1)
    private String message;
    @Basic
    @Column(name = "begin_date", nullable = false)
    private Timestamp beginDate;
    @Basic
    @Column(name = "update_date", nullable = false)
    private Timestamp updateDate;
    @Basic
    @Column(name = "level", nullable = true, length = 100)
    private String level;

    public logs(int id, Integer configId, String status, String message, Timestamp beginDate, Timestamp updateDate, String level) {
        this.id = id;
        this.configId = configId;
        this.status = status;
        this.message = message;
        this.beginDate = beginDate;
        this.updateDate = updateDate;
        this.level = level;
    }

    public logs(Integer configId, String status, String message, Timestamp beginDate, Timestamp updateDate, String level) {
        this.configId = configId;
        this.status = status;
        this.message = message;
        this.beginDate = beginDate;
        this.updateDate = updateDate;
        this.level = level;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getConfigId() {
        return configId;
    }

    public void setConfigId(Integer configId) {
        this.configId = configId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Timestamp beginDate) {
        this.beginDate = beginDate;
    }

    public Timestamp getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Timestamp updateDate) {
        this.updateDate = updateDate;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}
