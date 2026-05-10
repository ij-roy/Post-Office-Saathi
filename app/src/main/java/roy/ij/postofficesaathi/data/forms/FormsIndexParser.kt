package roy.ij.postofficesaathi.data.forms

import org.json.JSONArray
import roy.ij.postofficesaathi.domain.forms.FormItem

object FormsIndexParser {
    fun parse(json: String): List<FormItem> {
        val array = JSONArray(json)
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.getJSONObject(index)
                val keywords = item.optJSONArray("keywords")
                add(
                    FormItem(
                        id = item.getString("id"),
                        title = item.getString("title"),
                        category = item.optString("category", "Forms"),
                        description = item.optString("description", ""),
                        keywords = if (keywords == null) {
                            emptyList()
                        } else {
                            List(keywords.length()) { keywordIndex -> keywords.getString(keywordIndex) }
                        },
                        file = item.getString("file"),
                        language = item.optString("language", "English")
                    )
                )
            }
        }
    }
}
