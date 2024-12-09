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
    @Column(name = "process_id", nullable = true)
    private Integer processId;
    @Basic
    @Column(name = "message", nullable = true, length = -1)
    private String message;
    @Basic
    @Column(name = "insert_date", nullable = false)
    private Timestamp insertDate;
    @Basic
    @Column(name = "level", nullable = true, length = 100)
    private String level;

    public logs(Integer processId, String message, Timestamp insertDate, String level) {
        this.processId = processId;
        this.message = message;
        this.insertDate = insertDate;
        this.level = level;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Integer getProcessId() {
        return processId;
    }

    public void setProcessId(Integer processId) {
        this.processId = processId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Timestamp getInsertDate() {
        return insertDate;
    }

    public void setInsertDate(Timestamp insertDate) {
        this.insertDate = insertDate;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        logs that = (logs) o;

        if (id != that.id) return false;
        if (processId != null ? !processId.equals(that.processId) : that.processId != null) return false;
        if (message != null ? !message.equals(that.message) : that.message != null) return false;
        if (insertDate != null ? !insertDate.equals(that.insertDate) : that.insertDate != null) return false;
        if (level != null ? !level.equals(that.level) : that.level != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (processId != null ? processId.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (insertDate != null ? insertDate.hashCode() : 0);
        result = 31 * result + (level != null ? level.hashCode() : 0);
        return result;
    }
}
