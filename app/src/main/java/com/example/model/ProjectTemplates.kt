package com.example.model

import java.util.UUID

object ProjectTemplates {

    fun createBlankMobile(name: String = "Mobile Prototype"): Project {
        val artboardId = UUID.randomUUID().toString()
        val artboard = Artboard(
            id = artboardId,
            name = "Screen 1",
            width = 360f,
            height = 640f,
            backgroundColorHex = "#0F172A",
            isStartArtboard = true,
            elements = listOf(
                DesignElement(
                    name = "Welcome Header",
                    type = ElementType.TEXT,
                    x = 24f,
                    y = 80f,
                    width = 312f,
                    height = 40f,
                    text = "Welcome to PixelForge",
                    fontSize = 24f,
                    textColorHex = "#F8FAFC",
                    isBold = true,
                    textAlign = TextAlignOption.LEFT
                ),
                DesignElement(
                    name = "Subtext",
                    type = ElementType.TEXT,
                    x = 24f,
                    y = 126f,
                    width = 312f,
                    height = 40f,
                    text = "Tap any element to select, move, and edit properties.",
                    fontSize = 14f,
                    textColorHex = "#94A3B8",
                    textAlign = TextAlignOption.LEFT
                ),
                DesignElement(
                    name = "Feature Card",
                    type = ElementType.RECTANGLE,
                    x = 24f,
                    y = 190f,
                    width = 312f,
                    height = 140f,
                    fillColorHex = "#1E293B",
                    cornerRadius = 16f,
                    strokeColorHex = "#334155",
                    strokeWidth = 1f
                ),
                DesignElement(
                    name = "Card Title",
                    type = ElementType.TEXT,
                    x = 44f,
                    y = 214f,
                    width = 270f,
                    height = 28f,
                    text = "Interactive UI Studio",
                    fontSize = 18f,
                    textColorHex = "#F1F5F9",
                    isBold = true
                ),
                DesignElement(
                    name = "Action Button",
                    type = ElementType.BUTTON,
                    x = 24f,
                    y = 360f,
                    width = 312f,
                    height = 54f,
                    fillColorHex = "#6366F1",
                    cornerRadius = 14f,
                    text = "Get Started",
                    fontSize = 16f,
                    textColorHex = "#FFFFFF",
                    isBold = true
                )
            )
        )

        return Project(
            name = name,
            artboards = listOf(artboard),
            activeArtboardId = artboardId
        )
    }

    fun createAuthFlow(name: String = "Login & Onboarding Flow"): Project {
        val screen1Id = UUID.randomUUID().toString()
        val screen2Id = UUID.randomUUID().toString()
        val btnToLoginId = UUID.randomUUID().toString()
        val btnBackToWelcomeId = UUID.randomUUID().toString()

        val welcomeArtboard = Artboard(
            id = screen1Id,
            name = "Welcome Screen",
            width = 360f,
            height = 640f,
            backgroundColorHex = "#0F172A",
            isStartArtboard = true,
            elements = listOf(
                DesignElement(
                    name = "Brand Glow Circle",
                    type = ElementType.CIRCLE,
                    x = 130f,
                    y = 90f,
                    width = 100f,
                    height = 100f,
                    fillColorHex = "#6366F1",
                    opacity = 0.9f
                ),
                DesignElement(
                    name = "App Title",
                    type = ElementType.TEXT,
                    x = 24f,
                    y = 220f,
                    width = 312f,
                    height = 36f,
                    text = "Apex Studio",
                    fontSize = 28f,
                    textColorHex = "#F8FAFC",
                    isBold = true,
                    textAlign = TextAlignOption.CENTER
                ),
                DesignElement(
                    name = "Hero Tagline",
                    type = ElementType.TEXT,
                    x = 32f,
                    y = 265f,
                    width = 296f,
                    height = 48f,
                    text = "Design mobile interfaces directly on your device with lightning speed.",
                    fontSize = 14f,
                    textColorHex = "#94A3B8",
                    textAlign = TextAlignOption.CENTER
                ),
                DesignElement(
                    id = btnToLoginId,
                    name = "Login Button",
                    type = ElementType.BUTTON,
                    x = 32f,
                    y = 380f,
                    width = 296f,
                    height = 52f,
                    fillColorHex = "#6366F1",
                    cornerRadius = 14f,
                    text = "Continue to Login →",
                    fontSize = 16f,
                    textColorHex = "#FFFFFF",
                    isBold = true
                )
            ),
            connections = listOf(
                PrototypeConnection(
                    sourceElementId = btnToLoginId,
                    targetArtboardId = screen2Id
                )
            )
        )

        val loginArtboard = Artboard(
            id = screen2Id,
            name = "Login Screen",
            width = 360f,
            height = 640f,
            backgroundColorHex = "#0F172A",
            isStartArtboard = false,
            elements = listOf(
                DesignElement(
                    name = "Login Header",
                    type = ElementType.TEXT,
                    x = 24f,
                    y = 60f,
                    width = 312f,
                    height = 36f,
                    text = "Welcome Back",
                    fontSize = 24f,
                    textColorHex = "#FFFFFF",
                    isBold = true
                ),
                DesignElement(
                    name = "Email Field",
                    type = ElementType.RECTANGLE,
                    x = 24f,
                    y = 120f,
                    width = 312f,
                    height = 52f,
                    fillColorHex = "#1E293B",
                    cornerRadius = 12f,
                    strokeColorHex = "#334155",
                    strokeWidth = 1f
                ),
                DesignElement(
                    name = "Email Placeholder",
                    type = ElementType.TEXT,
                    x = 40f,
                    y = 135f,
                    width = 280f,
                    height = 24f,
                    text = "user@example.com",
                    fontSize = 14f,
                    textColorHex = "#64748B"
                ),
                DesignElement(
                    name = "Password Field",
                    type = ElementType.RECTANGLE,
                    x = 24f,
                    y = 190f,
                    width = 312f,
                    height = 52f,
                    fillColorHex = "#1E293B",
                    cornerRadius = 12f,
                    strokeColorHex = "#334155",
                    strokeWidth = 1f
                ),
                DesignElement(
                    name = "Password Placeholder",
                    type = ElementType.TEXT,
                    x = 40f,
                    y = 205f,
                    width = 280f,
                    height = 24f,
                    text = "••••••••••••",
                    fontSize = 16f,
                    textColorHex = "#64748B"
                ),
                DesignElement(
                    name = "Sign In Action",
                    type = ElementType.BUTTON,
                    x = 24f,
                    y = 270f,
                    width = 312f,
                    height = 52f,
                    fillColorHex = "#10B981",
                    cornerRadius = 12f,
                    text = "Sign In",
                    fontSize = 16f,
                    textColorHex = "#FFFFFF",
                    isBold = true
                ),
                DesignElement(
                    id = btnBackToWelcomeId,
                    name = "Back Button",
                    type = ElementType.BUTTON,
                    x = 24f,
                    y = 340f,
                    width = 312f,
                    height = 46f,
                    fillColorHex = "#1E293B",
                    cornerRadius = 12f,
                    text = "← Back to Welcome",
                    fontSize = 14f,
                    textColorHex = "#94A3B8"
                )
            ),
            connections = listOf(
                PrototypeConnection(
                    sourceElementId = btnBackToWelcomeId,
                    targetArtboardId = screen1Id
                )
            )
        )

        return Project(
            name = name,
            artboards = listOf(welcomeArtboard, loginArtboard),
            activeArtboardId = screen1Id
        )
    }

    fun createEcommerceFlow(name: String = "Shop Catalog & Detail"): Project {
        val catalogId = UUID.randomUUID().toString()
        val detailId = UUID.randomUUID().toString()
        val cardBtnId = UUID.randomUUID().toString()
        val backBtnId = UUID.randomUUID().toString()

        val catalogScreen = Artboard(
            id = catalogId,
            name = "Catalog Feed",
            width = 360f,
            height = 640f,
            backgroundColorHex = "#FFFFFF",
            isStartArtboard = true,
            elements = listOf(
                DesignElement(
                    name = "Store Header",
                    type = ElementType.TEXT,
                    x = 20f,
                    y = 40f,
                    width = 320f,
                    height = 36f,
                    text = "Nordic Living",
                    fontSize = 24f,
                    textColorHex = "#111827",
                    isBold = true
                ),
                DesignElement(
                    name = "Product Card Bg",
                    type = ElementType.RECTANGLE,
                    x = 20f,
                    y = 90f,
                    width = 320f,
                    height = 240f,
                    fillColorHex = "#F3F4F6",
                    cornerRadius = 18f
                ),
                DesignElement(
                    name = "Product Hero Image Block",
                    type = ElementType.RECTANGLE,
                    x = 40f,
                    y = 110f,
                    width = 280f,
                    height = 120f,
                    fillColorHex = "#E5E7EB",
                    cornerRadius = 12f
                ),
                DesignElement(
                    name = "Product Name",
                    type = ElementType.TEXT,
                    x = 40f,
                    y = 244f,
                    width = 280f,
                    height = 24f,
                    text = "Minimalist Armchair",
                    fontSize = 18f,
                    textColorHex = "#1F2937",
                    isBold = true
                ),
                DesignElement(
                    name = "Price Tag",
                    type = ElementType.TEXT,
                    x = 40f,
                    y = 274f,
                    width = 120f,
                    height = 24f,
                    text = "$249.00",
                    fontSize = 16f,
                    textColorHex = "#059669",
                    isBold = true
                ),
                DesignElement(
                    id = cardBtnId,
                    name = "View Details Button",
                    type = ElementType.BUTTON,
                    x = 200f,
                    y = 268f,
                    width = 120f,
                    height = 40f,
                    fillColorHex = "#111827",
                    cornerRadius = 10f,
                    text = "View Details",
                    fontSize = 13f,
                    textColorHex = "#FFFFFF"
                )
            ),
            connections = listOf(
                PrototypeConnection(
                    sourceElementId = cardBtnId,
                    targetArtboardId = detailId
                )
            )
        )

        val detailScreen = Artboard(
            id = detailId,
            name = "Product Detail",
            width = 360f,
            height = 640f,
            backgroundColorHex = "#FFFFFF",
            isStartArtboard = false,
            elements = listOf(
                DesignElement(
                    name = "Hero Photo Banner",
                    type = ElementType.RECTANGLE,
                    x = 0f,
                    y = 0f,
                    width = 360f,
                    height = 260f,
                    fillColorHex = "#E5E7EB",
                    cornerRadius = 0f
                ),
                DesignElement(
                    id = backBtnId,
                    name = "Back Arrow Button",
                    type = ElementType.BUTTON,
                    x = 16f,
                    y = 36f,
                    width = 80f,
                    height = 36f,
                    fillColorHex = "#FFFFFF",
                    cornerRadius = 8f,
                    text = "← Back",
                    fontSize = 13f,
                    textColorHex = "#111827"
                ),
                DesignElement(
                    name = "Detail Title",
                    type = ElementType.TEXT,
                    x = 20f,
                    y = 280f,
                    width = 320f,
                    height = 32f,
                    text = "Minimalist Armchair",
                    fontSize = 22f,
                    textColorHex = "#111827",
                    isBold = true
                ),
                DesignElement(
                    name = "Detail Price",
                    type = ElementType.TEXT,
                    x = 20f,
                    y = 320f,
                    width = 150f,
                    height = 28f,
                    text = "$249.00 USD",
                    fontSize = 18f,
                    textColorHex = "#059669",
                    isBold = true
                ),
                DesignElement(
                    name = "Description",
                    type = ElementType.TEXT,
                    x = 20f,
                    y = 360f,
                    width = 320f,
                    height = 70f,
                    text = "Crafted with solid oak frame and breathable wool upholstery for ultimate ergonomic relaxation.",
                    fontSize = 14f,
                    textColorHex = "#4B5563"
                ),
                DesignElement(
                    name = "Add to Cart Button",
                    type = ElementType.BUTTON,
                    x = 20f,
                    y = 460f,
                    width = 320f,
                    height = 54f,
                    fillColorHex = "#059669",
                    cornerRadius = 14f,
                    text = "Add to Cart • $249",
                    fontSize = 16f,
                    textColorHex = "#FFFFFF",
                    isBold = true
                )
            ),
            connections = listOf(
                PrototypeConnection(
                    sourceElementId = backBtnId,
                    targetArtboardId = catalogId
                )
            )
        )

        return Project(
            name = name,
            artboards = listOf(catalogScreen, detailScreen),
            activeArtboardId = catalogId
        )
    }
}
