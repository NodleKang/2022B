package provide.gson;

public class OutputQueueData {
    String result;

    public OutputQueueData(String result) {
        this.result = result;
    }

    public String getResult() {
        return this.result;
    }

    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = 31 * result + ((this.result == null) ? 0 : this.result.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OutputQueueData other = (OutputQueueData)obj;
        if (this.result == null) {
            if (other.result != null)
                return false;
        } else if (!this.result.equals(other.result)) {
            return false;
        }
        return true;
    }
}
