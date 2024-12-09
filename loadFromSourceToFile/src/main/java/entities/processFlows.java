package entities;

import jakarta.persistence.*;

@Entity
@Table(name = "process_flows", schema = "db_controller", catalog = "")
public class processFlows {
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "current_stage", nullable = false, length = 100)
    private String currentStage;
    @Basic
    @Column(name = "next_stage", nullable = true, length = 100)
    private String nextStage;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCurrentStage() {
        return currentStage;
    }

    public void setCurrentStage(String currentStage) {
        this.currentStage = currentStage;
    }

    public String getNextStage() {
        return nextStage;
    }

    public void setNextStage(String nextStage) {
        this.nextStage = nextStage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        processFlows that = (processFlows) o;

        if (id != that.id) return false;
        if (currentStage != null ? !currentStage.equals(that.currentStage) : that.currentStage != null) return false;
        if (nextStage != null ? !nextStage.equals(that.nextStage) : that.nextStage != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (currentStage != null ? currentStage.hashCode() : 0);
        result = 31 * result + (nextStage != null ? nextStage.hashCode() : 0);
        return result;
    }
}
