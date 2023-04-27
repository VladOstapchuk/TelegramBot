package academy.prog.javaprobot.webapp;

import lombok.Data;

@Data
public class dto {
        private final long count;
        private final int pageSize;

        private dto(long count, int pageSize) {
            this.count = count;
            this.pageSize = pageSize;
        }

        public static dto of(long count, int pageSize) {
            return new dto(count, pageSize);
        };
}
