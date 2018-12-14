## 支持的属性 

- app:isShowShadow `是否显示阴影`
- app:strokeSize `描边线条大小`
- app:strokeColor `描边颜色`
- app:bgColor `普通状态下的颜色`
- app:bgPressColor `click状态下的颜色`
- app:bgDisabledColor `disabled下的颜色`
- app:round `四个圆角的半径 优先使用此属性`
- app:topLeftRound `左上角的圆角大小`
- app:topRightRound `右上角的圆角大小`
- app:bottomLeftRound `左下角的圆角大小`
- app:bottomRightRound `右下脚的圆角大小`
- app:bgGradientStartColor `普通状态下渐变开始的颜色`
- app:bgGradientEndColor `普通状态下渐变结束的颜色`
- app:bgGradientDisabledStartColor `disabled状态下渐变开始的颜色`
- app:bgGradientDisabledEndColor`disabled状态下渐变结束的颜色`
- app:bgGradientPressStartColor `click状态下渐变开始的颜色`
- app:bgGradientPressEndColor `click状态下渐变结束的颜色`
- app:gradientStyle `渐变的样式`
    * linear 线性渐变
    * radial 圆形渐变
    * sweep  扇形渐变
- app:isClickRipple `是否显示click状态的波纹效果`

---

> 如果只设置普通状态的颜色或者渐变颜色，会自动生成click状态和disabled状态的颜色

> 渐变状态下可以如果感觉click的颜色不太对可以禁用波纹效果(app:isClickRipple="false")




![image](https://github.com/zhongqiang1995/ChangeButton/blob/master/g.gif)