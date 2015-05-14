package id.co.technomotion.androidforensicapp.model;

public class DbFileInfo implements Item{
        String name;
        String path;

        public DbFileInfo(String name, String path) {
            this.name = name;
            this.path = path;
        }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    @Override
        public String toString() {
            return "DbFileInfo{" +
                    "name='" + name + '\'' +
                    ", path='" + path + '\'' +
                    '}';
        }

    @Override
    public boolean isGroup() {
        return false;
    }
}