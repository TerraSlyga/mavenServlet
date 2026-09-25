package sumdu.edu.ua.core.domain;

import java.util.List;

public class Page<T> {
    private final List<T> items;
    private final PageRequest request;
    private final long total;

    public Page(List<T> items, PageRequest request, long total) {
        this.items = items;
        this.request = request;
        this.total = total;
    }

    public List<T> getItems() {
        return items;
    }

    public PageRequest getRequest() {
        return request;
    }

    public long getTotal() {
        return total;
    }

    public int getTotalPages() {
        return request.getSize() == 0 ? 1 : (int) Math.ceil((double) total / request.getSize());
    }

    public boolean hasNext() {
        return request.getPage() + 1 < getTotalPages();
    }

    public boolean hasPrevious() {
        return request.getPage() > 0;
    }
}
