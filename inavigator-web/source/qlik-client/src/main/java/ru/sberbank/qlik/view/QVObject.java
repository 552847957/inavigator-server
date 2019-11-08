package ru.sberbank.qlik.view;

public class QVObject {
    public enum Type {
        Unknown(0),//=Unknown
        List(1),//=List Box
        Multi(2),//=Multi Box
        Statistics(3),//=Statistics Box
        Table(4),//=Table Box
        Button(5),//=Button
        Text(6),//=Text Object
        Current(7),//=Current Selections Box
        Input(8),//=Input Box
        LineObject(9),//=Line/Arrow Object
        Pivot(10),//=Pivot Table
        Straight(11),//=Straight Table
        Bar(12),//=Bar Chart
        Pie(13),//=Pie Chart
        Scatter(14),//=Scatter Chart
        Line(15),//=Line Chart
        Combo(16),//=Combo Chart
        Custom(17),//=Custom Object
        BookmarkObject(18),//=Bookmark Object
        SliderObject(19),//=Slider/Calendar Object
        Grid(20),//=Grid Chart
        Radar(21),//=Radar Chart
        Gauge(22),//=Gauge Chart
        Chart(23),//=Chart Derived
        Sheet(24),//=Sheet
        Tabrow(25),//=Tabrow
        Document(26),//=Document
        Block(27),//=Block Chart
        Funnel(28),//=Funnel Chart
        Internal(29),//=Internal Layout
        Inter(30),//=Inter Sheet Transfer
        Search(31),//=Search Object
        Bookmark(32),//=Bookmark
        Report(33),//=Report
        Slider(34),//=Slider
        Calendar(35);//=Calendar

        private final int id;

        Type(int id) {
            this.id = id;
        }

        public static QVObject.Type resolveById(int id) {
            for (QVObject.Type type : values()) {
                if (type.id == id) return type;
            }
            return Unknown;
        }
    }
}
