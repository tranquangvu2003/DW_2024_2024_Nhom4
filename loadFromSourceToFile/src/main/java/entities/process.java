package entities;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table(name = "process", schema = "db_controller", catalog = "")
public class process {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "config_id", nullable = true)
    private Integer configId;
    @Basic
    @Column(name = "process_at", nullable = true, length = 100)
    private String processAt;
    @Basic
    @Column(name = "status", nullable = true, length = 100)
    private String status;
    @Basic
    @Column(name = "begin_date", nullable = false)
    private Timestamp beginDate;
    @Basic
    @Column(name = "update_date", nullable = false)
    private Timestamp updateDate;

    public process(int id, Integer configId, String processAt, String status, Timestamp beginDate, Timestamp updateDate) {
        this.id = id;
        this.configId = configId;
        this.processAt = processAt;
        this.status = status;
        this.beginDate = beginDate;
        this.updateDate = updateDate;
    }

    public process(Integer configId, String processAt, String status, Timestamp beginDate, Timestamp updateDate) {
        this.configId = configId;
        this.processAt = processAt;
        this.status = status;
        this.beginDate = beginDate;
        this.updateDate = updateDate;
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

    public String getProcessAt() {
        return processAt;
    }

    public void setProcessAt(String processAt) {
        this.processAt = processAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        process that = (process) o;

        if (id != that.id) return false;
        if (configId != null ? !configId.equals(that.configId) : that.configId != null) return false;
        if (processAt != null ? !processAt.equals(that.processAt) : that.processAt != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (beginDate != null ? !beginDate.equals(that.beginDate) : that.beginDate != null) return false;
        if (updateDate != null ? !updateDate.equals(that.updateDate) : that.updateDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (configId != null ? configId.hashCode() : 0);
        result = 31 * result + (processAt != null ? processAt.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (beginDate != null ? beginDate.hashCode() : 0);
        result = 31 * result + (updateDate != null ? updateDate.hashCode() : 0);
        return result;
    }
}
