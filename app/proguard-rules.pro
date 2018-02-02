-keep class .R
-keep class **.R$* { <fields>; }
-keepclasseswithmembers class **.R$* { public static final int define_*; }

-dontnote com.mikepenz.fastadapter.items.**

-dontwarn android.arch.persistence.room.paging.LimitOffsetDataSource