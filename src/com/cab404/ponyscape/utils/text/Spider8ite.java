package com.cab404.ponyscape.utils.text;

import java.util.List;

/**
 * M4K3S QUIRKS
 * Created at 18:34 on 08.01.15
 *
 * @author cab404
 */
public class Spider8ite {

    public static interface TextPr0cess0r {
        public StringBuilder apply(StringBuilder builder);
    }

    public static class Template {
        private final List<TextPr0cess0r> algorithm;

        private Template(List<TextPr0cess0r> algorithm) {
            this.algorithm = algorithm;
        }

        private String apply(String what) {
            StringBuilder builder = new StringBuilder(what);

            for (TextPr0cess0r tp : algorithm)
                tp.apply(builder);

            return builder.toString();
        }

    }

    public static class ReplaceTPr implements TextPr0cess0r {
        private final String what;
        private final String to_what;

        public ReplaceTPr(String what, String to_what) {
            this.what = what;
            this.to_what = to_what;
        }

        @Override
        public StringBuilder apply(StringBuilder builder) {
            int i;
            while ((i = builder.indexOf(what)) != -1)
                builder.replace(i, i + what.length(), to_what);
            return builder;
        }
    }

    public static class Precedence implements TextPr0cess0r {

        private CharSequence start;

        public Precedence(CharSequence start) {
            this.start = start;
        }

        @Override
        public StringBuilder apply(StringBuilder builder) {
            return builder.insert(0, start);
        }
    }

    public static class Postinsertion implements TextPr0cess0r {

        private CharSequence end;

        public Postinsertion(CharSequence end) {
            this.end = end;
        }

        @Override
        public StringBuilder apply(StringBuilder builder) {
            return builder.append(end);
        }

    }

}
