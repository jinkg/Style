# for component
-keep public class com.yalin.style.engine.StyleWallpaperProxy{
    *;
}
-keep public class com.yalin.style.engine.GLWallpaperServiceProxy{
    *;
}
-keep public class com.yalin.style.engine.GLWallpaperServiceProxy$GLActiveEngine{
    *;
}
-keep public class com.yalin.style.engine.WallpaperServiceProxy{
    *;
}
-keep public class com.yalin.style.engine.WallpaperServiceProxy$ActiveEngine{
    *;
}
-keep public class com.yalin.style.engine.GDXWallpaperServiceProxy{
    *;
}
-keep public class com.yalin.style.engine.GDXWallpaperServiceProxy$GDXActiveEngine{
    *;
}
-keep interface com.yalin.style.engine.IProvider{
    *;
}
-keep class android.support.v4.content.ContextCompat{
    *;
}
-keep class android.support.v4.content.res.ResourcesCompat{
    *;
}
-keep class android.support.v4.graphics.drawable.DrawableCompat{
    *;
}
-keep class android.support.graphics.drawable.VectorDrawableCompat{
    *;
}

-dontwarn com.badlogic.gdx.backends.android.AndroidFragmentApplication
-dontwarn com.badlogic.gdx.utils.GdxBuild
-dontwarn com.badlogic.gdx.physics.box2d.utils.Box2DBuild
-dontwarn com.badlogic.gdx.jnigen.BuildTarget*
-keep interface com.badlogic.gdx.**{
    *;
}
-keep class com.badlogic.gdx.**{
    *;
}
