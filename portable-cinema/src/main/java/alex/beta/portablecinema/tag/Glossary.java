package alex.beta.portablecinema.tag;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;
import java.util.Set;

/**
 * Default glossary is defined in Glossary.json, which is put in the root folder.
 * Sample glossary:
 * <pre>
 * {
 *   "actors":"name1, name2, name3, name4",
 *   "keywords":[
 *     {
 *       "text":"keyword1",
 *       "alias":[
 *         "alias11 of keyword1",
 *         "alias12 of keyword1"
 *       ],
 *       "tags":[
 *         "tag11 of keyword1",
 *         "tag12 of keyword1"
 *       ]
 *     },
 *     {
 *       "text":"keyword2",
 *       "alias":[
 *         "alias21 of keyword2"
 *       ],
 *       "tags":[
 *         "tag21 of keyword2"
 *       ]
 *     }
 *   ]
 * }
 * </pre>
 */

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Glossary {
    public static final String DELIMITER = ",";
    private String actors;
    private Term[] keywords;

    public static class Term {
        private String text;
        private Set<String> alias;
        private Set<String> tags;

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Set<String> getAlias() {
            return alias;
        }

        public void setAlias(Set<String> alias) {
            this.alias = alias;
        }

        public Set<String> getTags() {
            return tags;
        }

        public void setTags(Set<String> tags) {
            this.tags = tags;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Term term1 = (Term) o;
            return Objects.equals(text, term1.text);
        }

        @Override
        public int hashCode() {
            return Objects.hash(text);
        }

        @Override
        public String toString() {
            return "Term{" +
                    "text='" + text + '\'' +
                    ", alias=" + alias +
                    ", tags=" + tags +
                    '}';
        }
    }
}
