public class Student {
    private int id;
    private String name;
    private String department;
    private int[] marks; 

    public Student(int id, String name, String department, int[] marks) {
        this.id = id;
        this.name = name;
        this.department = department;
        this.marks = marks.clone();
    }

    public int getId() {
         return id;
         }
    public String getName() {
         return name;
         }
    public void setName(String name) { 
        this.name = name;
     }
    public String getDepartment() {
         return department;
         }
    public void setDepartment(String department) { 
        this.department = department; 
    }
    public int[] getMarks() { 
        return marks.clone(); 
    }
    public void setMarks(int[] marks) {
        this.marks = marks.clone();
    }

    public double getAverage() {
        int sum = 0;
        for (int m : marks) sum += m;
        return sum / 5.0;
    }

    public int getHighest() {
        int max = marks[0];
        for (int i = 1; i < marks.length; i++) if (marks[i] > max) max = marks[i];
        return max;
    }

    public int getLowest() {
        int min = marks[0];
        for (int i = 1; i < marks.length; i++) if (marks[i] < min) min = marks[i];
        return min;
    }

    public boolean passedAll() {
        for (int m : marks) if (m < 50) return false;
        return true;
    }

    public int[] getSortedMarks() {
        int[] sorted = marks.clone();
        for (int i = 0; i < sorted.length - 1; i++) {
            for (int j = 0; j < sorted.length - 1 - i; j++) {
                if (sorted[j] > sorted[j+1]) {
                    int temp = sorted[j];
                    sorted[j] = sorted[j+1];
                    sorted[j+1] = temp;
                }
            }
        }
        return sorted;
    }

    @Override
    public String toString() {
        return String.format("ID:%d | Name:%s | Dept:%s | Marks:%s | Avg:%.2f | Max:%d Min:%d | %s",
                id, name, department, java.util.Arrays.toString(marks), getAverage(), getHighest(), getLowest(),
                passedAll()? "PASSED" : "FAILED");
    }
}