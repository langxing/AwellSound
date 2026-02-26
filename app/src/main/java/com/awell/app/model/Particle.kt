package com.awell.app.model

class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var size: Float,
    var color: Int
) {
    var alpha: Int = 255
    // 随机的消失速度，让粒子群产生层次感
    private val fadeSpeed = (Math.random() * 3 + 3).toInt()

    fun update() {
        x += vx
        y += vy
        // 模拟阻尼，让粒子越飞越慢
        vx *= 0.94f
        vy *= 0.94f

        alpha -= fadeSpeed
        if (alpha < 0) alpha = 0
    }

    fun isAlive(): Boolean = alpha > 0
}