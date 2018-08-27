package com.ttcompany.trongtuyen.carmap.utils

import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.ttcompany.trongtuyen.carmap.R
import com.ttcompany.trongtuyen.carmap.controllers.AppController
import com.ttcompany.trongtuyen.carmap.models.Geometry
import com.ttcompany.trongtuyen.carmap.models.User
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by tuyen on 08/05/2018.
 */

object Helper {
//    fun getProductState(state: Int): String {
//        when (state) {
//            0 -> return "Còn hàng"
//            1 -> return "Hết hàng"
//            2 -> return "Đang nhập"
//            3 -> return "Ngừng kinh doanh"
//        }
//        return "Không xác định"
//    }
//
//    fun getRating(rating: Double?): Float {
//        return if (rating != null && rating > 0) rating.toFloat() else 0f
//
//    }
//
//    fun getDiscountPercent(promotionPrice: Int?, price: Int?): String {
//        return if (promotionPrice == null || price == null) "null" else "(-" + Math.round((price - promotionPrice).toFloat() / price * 100) + "%)"
//    }
//
//    fun getDiscountPercentWithoutBracket(promotionPrice: Int?, price: Int?): String {
//        return if (promotionPrice == null || price == null) "null" else "" + Math.round((price - promotionPrice).toFloat() / price * 100) + "%"
//    }
//
//    fun formatTextNReviews(value: Int): String {
//        return if (value == 0) {
//            "Chưa có đánh giá"
//        } else value.toString() + " Đánh giá"
//    }
//
//    fun formatTextNReviews2(value: Int): String {
//        return "($value Đánh giá)"
//    }

//    fun bindingPromotePriceAndPrice(txtPrice: TextView, txtOriginPrice: TextView, txtDiscountPercent: TextView?, price: Int?, promotionPrice: Int?) {
//        if (promotionPrice != null && promotionPrice > 0) {
//            txtOriginPrice.visibility = View.VISIBLE
//            txtOriginPrice.setText(CurrencyFormatter.format(price))
//            txtPrice.setText(CurrencyFormatter.format(promotionPrice))
//
//            if (txtDiscountPercent != null) {
//                txtDiscountPercent.visibility = View.VISIBLE
//                txtDiscountPercent.text = Helper.getDiscountPercent(promotionPrice, price)
//            }
//        } else {
//            txtOriginPrice.visibility = View.GONE
//            txtPrice.setText(CurrencyFormatter.format(price))
//            if (txtDiscountPercent != null) {
//                txtDiscountPercent.visibility = View.GONE
//            }
//        }
//    }

//    fun bindingPromotePriceAndPriceSpecial(txtPrice: TextView, txtOriginPrice: TextView, txtDiscountPercent: TextView?, price: Int?, promotionPrice: Int?) {
//        if (promotionPrice != null && promotionPrice > 0) {
//            txtOriginPrice.visibility = View.VISIBLE
//            txtOriginPrice.setText(CurrencyFormatter.format(price))
//            txtPrice.setText(CurrencyFormatter.format(promotionPrice))
//
//            if (txtDiscountPercent != null) {
//                txtDiscountPercent.visibility = View.VISIBLE
//                txtDiscountPercent.text = Helper.getDiscountPercentWithoutBracket(promotionPrice, price)
//            }
//        } else {
//            txtOriginPrice.visibility = View.GONE
//            txtPrice.setText(CurrencyFormatter.format(price))
//            if (txtDiscountPercent != null) {
//                txtDiscountPercent.visibility = View.GONE
//            }
//        }
//    }
//
//    fun bindingAvgRatingAndReviewText(ratingBarProductAvgRating: SimpleRatingBar, txtProductAvgRating: TextView, txtProductNReviews: TextView, avgRating: Double?, nReviews: Int?) {
//        if (avgRating != null && avgRating > 0) {
//            ratingBarProductAvgRating.setRating(avgRating.toFloat())
//            txtProductAvgRating.text = formatAvgRating(avgRating)
//        } else {
//            ratingBarProductAvgRating.setRating(0)
//            txtProductAvgRating.text = "0"
//        }
//        txtProductNReviews.text = Helper.formatTextNReviews(nReviews!!)
//    }
//
//    fun bindingAvgRatingAndReviewText2(ratingBarProductAvgRating: SimpleRatingBar, txtProductAvgRating: TextView, txtProductNReviews: TextView, avgRating: Double?, nReviews: Int?) {
//        if (avgRating != null && avgRating > 0) {
//            ratingBarProductAvgRating.setRating(avgRating.toFloat())
//            txtProductAvgRating.text = formatAvgRating(avgRating)
//        } else {
//            ratingBarProductAvgRating.setRating(0)
//            txtProductAvgRating.text = "0"
//        }
//        txtProductNReviews.text = Helper.formatTextNReviews2(nReviews!!)
//    }
//
//    fun bindingAvgRatingAndReview(ratingBarProductAvgRating: SimpleRatingBar, txtProductAvgRating: TextView?, txtProductNReviews: TextView, avgRating: Double?, nReviews: Int?) {
//        if (avgRating != null && avgRating > 0) {
//            ratingBarProductAvgRating.setRating(avgRating.toFloat())
//            if (txtProductAvgRating != null)
//                txtProductAvgRating.text = formatAvgRating(avgRating)
//        } else {
//            ratingBarProductAvgRating.setRating(0)
//
//            if (txtProductAvgRating != null)
//                txtProductAvgRating.text = "0"
//        }
//
//        if (nReviews != null) {
//            txtProductNReviews.text = Helper.formatReview(nReviews)
//        } else {
//            txtProductNReviews.text = Helper.formatReview(0)
//        }
//    }
//
//    fun formatAvgRating(avgRating: Double): String {
//        return round(avgRating, 1).toString() + ""
//    }
//
//    fun round(d: Double, decimalPlace: Int): Float {
//        var bd = BigDecimal(java.lang.Double.toString(d))
//        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP)
//        return bd.toFloat()
//    }
//
//    fun formatReview(value: Int): String {
//        return "($value)"
//    }
//
//    fun formatRating(avgRating: Double?): String {
//        return if (avgRating != null && avgRating > 0) avgRating.toFloat().toString() + "" else "0"
//    }
//
//    fun bindingStoreRefInfo(context: Context, storeAvatar: ImageView, txtShopName: TextView, iconVerified: ImageView, avatar: String?, name: String, isVerified: Boolean) {
//        txtShopName.text = name
//        if (isVerified)
//            iconVerified.visibility = View.VISIBLE
//        else
//            iconVerified.visibility = View.GONE
//
//        if (avatar != null) {
//            GlideApp.with(context)
//                    .load(LinkHelper.getAbsPublicLink(avatar))
//                    .centerCrop()
//                    .placeholder(R.drawable.store_default_avatar)
//                    .into(storeAvatar)
//        } else {
//            storeAvatar.setImageResource(R.drawable.store_default_avatar)
//        }
//    }

//    fun loadAvatar(context: Context, ivAvatar: ImageView, avatar: String?, defaultImage: Int) {
//        if (avatar != null) {
//            GlideApp.with(context)
//                    .load(LinkHelper.getAbsPublicLink(avatar))
//                    .centerCrop()
//                    .placeholder(defaultImage)
//                    .into(ivAvatar)
//        } else {
//            ivAvatar.setImageResource(defaultImage)
//        }
//    }
//
//    fun loadAvatarWithoutPlaceHolder(context: Context, ivAvatar: ImageView, avatar: String?, defaultImage: Int) {
//        if (avatar != null) {
//            GlideApp.with(context)
//                    .load(LinkHelper.getAbsPublicLink(avatar))
//                    .centerCrop()
//                    .into(ivAvatar)
//        } else {
//            ivAvatar.setImageResource(defaultImage)
//        }
//    }
//
//    fun loadAvatarWithoutPlaceHolderAbsLink(context: Context, ivAvatar: ImageView, avatar: String?, defaultImage: Int) {
//        if (avatar != null) {
//            GlideApp.with(context)
//                    .load(avatar)
//                    .centerCrop()
//                    .into(ivAvatar)
//        } else {
//            ivAvatar.setImageResource(defaultImage)
//        }
//    }
//
//    fun visibleOrGone(isVisible: Boolean, view: View) {
//        if (!isVisible) {
//            view.visibility = View.GONE
//        } else {
//            view.visibility = View.VISIBLE
//        }
//    }
//
//    fun getNFollowersText(nFollowers: Int?): String {
//        return if (nFollowers == null || nFollowers <= 0) {
//            "Chưa có người theo dõi nào"
//        } else "Có " + TextHelper.groupThreeDigitDot(nFollowers) + " người theo dõi"
//
//    }
//
//    fun getPhoneNumber(phoneNumbers: List<PhoneNumber>?): String {
//        return if (phoneNumbers != null && phoneNumbers.size > 0) {
//            phoneNumbers[0].getContent()
//        } else "null"
//    }
//
//    fun bindingVerifiedStore(isVerified: Boolean, iconVerified: ImageView) {
//        if (isVerified) {
//            iconVerified.visibility = View.VISIBLE
//        } else {
//            iconVerified.visibility = View.GONE
//        }
//    }
//
//    fun loadStoreCoverWithoutPlaceHolder(context: Context, ivCover: ImageView, cover: String?, defaultImage: Int) {
//        if (cover != null) {
//            GlideApp.with(context)
//                    .load(LinkHelper.getAbsPublicLink(cover))
//                    .centerCrop()
//                    .into(ivCover)
//        } else {
//            ivCover.setImageResource(defaultImage)
//        }
//    }
//
//    fun formatTimeAgoFromMongoDB_GTM_0_VN(strDateTime: String): String {
//        return TimeAgo.formatVN_GMT0(TimeHelper.fromMongoDBDateString(strDateTime))
//    }
//
//    fun formatFullAddress(address: Address?): String {
//        return if (address == null) "null" else address!!.getFullAddress()
//
//    }
//
//    fun getFormatDistanceToPos(geometry: Geometry?): String {
//        if (geometry == null || AppController.userLocation == null) {
//            return "null"
//        }
//
//        val lng = geometry!!.getCoordinates().get(0)
//        val lat = geometry!!.getCoordinates().get(1)
//
//        val userLat = AppController.userLocation.getLatitude()
//        val userLng = AppController.userLocation.getLongitude()
//
//        val distanceInMeter = calcDistance(lat, lng, userLat, userLng)
//        return if (distanceInMeter < 1000) String.format("%.0f m", distanceInMeter) else String.format("%.2f km", distanceInMeter / 1000)
//
//    }
//
//    fun formatDistance(distanceInMeter: Double?): String {
//        if (distanceInMeter == null)
//            return "null"
//
//        return if (distanceInMeter < 1000) String.format("%.0f m", distanceInMeter) else String.format("%.2f km", distanceInMeter / 1000)
//
//    }
//
//    // Return distance in meters
//    fun calcDistance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
//        val Radius = 6371000.0// meter
//        val dLat = Math.toRadians(lat1 - lat2)
//        val dLon = Math.toRadians(lng1 - lng2)
//        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + (Math.cos(Math.toRadians(lat1))
//                * Math.cos(Math.toRadians(lat2))
//                * Math.sin(dLon / 2) * Math.sin(dLon / 2))
//        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
//
//        return Radius * c
//    }
//
//    // Return distance in meters
//    fun calcDistance(location1: LatLng, location2: LatLng): Double {
//
//
//        return calcDistance(location1.latitude, location1.longitude, location2.latitude, location2.longitude)
//    }
//
//    fun getNReviews(ratings: List<Int>): Int {
//        return ratings[0] + ratings[1] + ratings[2] + ratings[3] + ratings[4]
//    }
//
//    fun getAvgRating(ratings: List<Int>): Double {
//        return if (getNReviews(ratings) == 0) {
//            0.0
//        } else (ratings[0] * 1 + ratings[1] * 2 + ratings[2] * 3 + ratings[3] * 4 + ratings[4] * 5).toDouble() / getNReviews(ratings)
//    }
//
//    fun getUserName(user: User): String {
//        return user.getName()
//    }
//
//    fun formatDateFromMongoDBGTM0(strDateTime: String): String {
//        val date = TimeHelper.fromMongoDBDateString(strDateTime) ?: return "null"
//
//        val timestamp = date.getTime() + TimeZone.getDefault().rawOffset
//        val date2 = Date(timestamp)
//        val df2 = SimpleDateFormat("dd/MM/yy")
//        return df2.format(date2)
//    }
//
//    fun cropCircleBitmap(bitmap: Bitmap): Bitmap {
//        val output = Bitmap.createBitmap(bitmap.width,
//                bitmap.height, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(output)
//        val color = -0xbdbdbe
//        val paint = Paint()
//        val rect = Rect(0, 0, bitmap.width,
//                bitmap.height)
//
//        paint.isAntiAlias = true
//        canvas.drawARGB(0, 0, 0, 0)
//        // paint.setColor(color);
//        canvas.drawCircle((bitmap.width / 2).toFloat(),
//                (bitmap.height / 2).toFloat(), (bitmap.width / 2).toFloat(), paint)
//        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
//        canvas.drawBitmap(bitmap, rect, rect, paint)
//        return output
//    }
//
//
//    fun cropCircleBitmap(bitmap: Bitmap, width: Int, height: Int): Bitmap {
//        var bitmap = bitmap
//        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
//
//        val output = Bitmap.createBitmap(bitmap.width,
//                bitmap.height, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(output)
//        val color = -0xbdbdbe
//        val paint = Paint()
//        val rect = Rect(0, 0, bitmap.width,
//                bitmap.height)
//
//        paint.isAntiAlias = true
//        canvas.drawARGB(0, 0, 0, 0)
//        // paint.setColor(color);
//        canvas.drawCircle((bitmap.width / 2).toFloat(),
//                (bitmap.height / 2).toFloat(), (bitmap.width / 2).toFloat(), paint)
//        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
//        canvas.drawBitmap(bitmap, rect, rect, paint)
//        return output
//    }
//
//    fun loadImageToMarker(context: Context, marker: Marker, imageURL: String?) {
//        if (imageURL == null) {
//            val circleIcon = cropCircleBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.store_default_avatar), 67, 67)
//            marker.setIcon(BitmapDescriptorFactory.fromBitmap(circleIcon))
//            return
//        }
//
//        GlideApp.with(context)
//                .asBitmap()
//                .load(LinkHelper.getAbsPublicLink(imageURL))
//                .error(R.drawable.store_default_avatar)
//                .placeholder(R.drawable.store_default_avatar)
//                .override(67, 67)
//                .circleCrop()
//                .into(object : SimpleTarget<Bitmap>() {
//                    fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>) {
//                        marker.setIcon(BitmapDescriptorFactory.fromBitmap(resource))
//                    }
//                })
//    }
//
//    fun bindingOpenTime(context: Context, weekOpenTime: WeekOpenTime, txtOpenTime: TextView, txtOpenTimeStatus: TextView) {
//        // Hom nay la thu may?
//        val now = Date()
//        val c = Calendar.getInstance()
//        c.time = now
//        val dayOfWeek = c.get(Calendar.DAY_OF_WEEK)
//
//        // Bay gio la` may gio`
//        val hour = c.get(Calendar.HOUR_OF_DAY) // gets hour in 24h format
//        val min = c.get(Calendar.MINUTE) // gets hour in 24h format
//
//        // Process
//        val openTime = weekOpenTime.getOpenTimeByDayOfWeeks(dayOfWeek)
//        if (openTime.getOpen() === openTime.getClose()) {
//            txtOpenTimeStatus.text = "ĐÓNG CỬA"
//            txtOpenTimeStatus.setTextColor(ContextCompat.getColor(context, R.color.secondary_text))
//            txtOpenTime.text = "Không làm việc"
//            return
//        }
//
//        txtOpenTime.text = String.format("%s - %s", getHourFormat(openTime.getOpen()), getHourFormat(openTime.getClose()))
//        val totalMin = hour * 60 + min
//        if (totalMin >= openTime.getOpen() * 60 && totalMin <= openTime.getClose() * 60) {
//            txtOpenTimeStatus.text = "ĐANG MỞ CỬA"
//            txtOpenTimeStatus.setTextColor(ContextCompat.getColor(context, R.color.primary))
//        } else {
//            txtOpenTimeStatus.text = "CHƯA MỞ CỬA"
//            txtOpenTimeStatus.setTextColor(ContextCompat.getColor(context, R.color.secondary_text))
//        }
//    }
//
//    private fun getHourFormat(hour: Double): String {
//        return String.format("%02d:%02d", hour.toInt(), (hour * 60).toInt() % 60)
//    }
//
//    fun getDeepWebsiteFromStoreDetail(store: StoreDetail): String? {
//        var website = store.getWebsite()
//        if (website == null && store.getBrandID() != null)
//            website = store.getBrandID().getWebsite()
//        return website
//    }
//
//    fun formatNViews(nViews: Int?): String {
//        return if (nViews == null) "null" else nViews.toString() + " lượt xem"
//    }
}