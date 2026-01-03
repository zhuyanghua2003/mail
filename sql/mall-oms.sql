/*
 Navicat Premium Dump SQL

 Source Server         : linux_docker_fenghuo
 Source Server Type    : MySQL
 Source Server Version : 50724 (5.7.24)
 Source Host           : 192.168.6.128:3306
 Source Schema         : mall-oms

 Target Server Type    : MySQL
 Target Server Version : 50724 (5.7.24)
 File Encoding         : 65001

 Date: 03/01/2026 14:08:27
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for oms_order
-- ----------------------------
DROP TABLE IF EXISTS `oms_order`;
CREATE TABLE `oms_order`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `member_id` bigint(20) NULL DEFAULT NULL COMMENT 'member_id',
  `order_sn` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?????',
  `coupon_id` bigint(20) NULL DEFAULT NULL COMMENT 'ʹ?õ??Ż?ȯ',
  `create_time` datetime NULL DEFAULT NULL COMMENT 'create_time',
  `member_username` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?û???',
  `total_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '?????ܶ',
  `pay_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT 'Ӧ???ܶ',
  `freight_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '?˷ѽ',
  `promotion_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '?????Ż?????????ۡ??????????ݼۣ?',
  `integration_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '???ֵֿ۽',
  `coupon_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '?Ż?ȯ?ֿ۽',
  `discount_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '??̨????????ʹ?õ??ۿ۽',
  `pay_type` tinyint(4) NULL DEFAULT NULL COMMENT '֧????ʽ??1->֧??????2->΢?ţ?3->?????? 4->?????????',
  `source_type` tinyint(4) NULL DEFAULT NULL COMMENT '??????Դ[0->PC??????1->app????]',
  `status` tinyint(4) NULL DEFAULT NULL COMMENT '????״̬??0->?????1->????????2->?ѷ?????3->?????ɣ?4->?ѹرգ?5->??Ч??????',
  `delivery_company` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??????˾(???ͷ?ʽ)',
  `delivery_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '???????',
  `auto_confirm_day` int(11) NULL DEFAULT NULL COMMENT '?Զ?ȷ??ʱ?䣨?죩',
  `integration` int(11) NULL DEFAULT NULL COMMENT '???Ի??õĻ??',
  `growth` int(11) NULL DEFAULT NULL COMMENT '???Ի??õĳɳ?ֵ',
  `bill_type` tinyint(4) NULL DEFAULT NULL COMMENT '??Ʊ????[0->??????Ʊ??1->???ӷ?Ʊ??2->ֽ?ʷ?Ʊ]',
  `bill_header` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??Ʊ̧ͷ',
  `bill_content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??Ʊ???',
  `bill_receiver_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??Ʊ?˵绰',
  `bill_receiver_email` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??Ʊ?????',
  `receiver_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?ջ???????',
  `receiver_phone` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?ջ??˵绰',
  `receiver_post_code` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?ջ????ʱ',
  `receiver_province` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ʡ??/ֱϽ?',
  `receiver_city` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '???',
  `receiver_region` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??',
  `receiver_detail_address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??ϸ??ַ',
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??????ע',
  `confirm_status` tinyint(4) NULL DEFAULT NULL COMMENT 'ȷ???ջ?״̬[0->δȷ?ϣ?1->??ȷ??]',
  `delete_status` tinyint(4) NULL DEFAULT NULL COMMENT 'ɾ??״̬??0->δɾ????1->??ɾ????',
  `use_integration` int(11) NULL DEFAULT NULL COMMENT '?µ?ʱʹ?õĻ??',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '֧??ʱ?',
  `delivery_time` datetime NULL DEFAULT NULL COMMENT '????ʱ?',
  `receive_time` datetime NULL DEFAULT NULL COMMENT 'ȷ???ջ?ʱ?',
  `comment_time` datetime NULL DEFAULT NULL COMMENT '????ʱ?',
  `modify_time` datetime NULL DEFAULT NULL COMMENT '?޸?ʱ?',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '????' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order
-- ----------------------------
INSERT INTO `oms_order` VALUES (1, 2, '202512172342205352001317043652702209', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, '谷雨', '15005898880', '1', '1', '1', '1', '1', NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for oms_order_item
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_item`;
CREATE TABLE `oms_order_item`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_id` bigint(20) NULL DEFAULT NULL COMMENT 'order_id',
  `order_sn` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'order_sn',
  `spu_id` bigint(20) NULL DEFAULT NULL COMMENT 'spu_id',
  `spu_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'spu_name',
  `spu_pic` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'spu_pic',
  `spu_brand` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'Ʒ?',
  `category_id` bigint(20) NULL DEFAULT NULL COMMENT '??Ʒ????id',
  `sku_id` bigint(20) NULL DEFAULT NULL COMMENT '??Ʒsku???',
  `sku_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??Ʒsku???',
  `sku_pic` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??ƷskuͼƬ',
  `sku_price` decimal(18, 4) NULL DEFAULT NULL COMMENT '??Ʒsku?۸',
  `sku_quantity` int(11) NULL DEFAULT NULL COMMENT '??Ʒ??????????',
  `sku_attrs_vals` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??Ʒ???????????ϣ?JSON??',
  `promotion_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '??Ʒ?????ֽ??',
  `coupon_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '?Ż?ȯ?Żݷֽ??',
  `integration_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '?????Żݷֽ??',
  `real_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '????Ʒ?????Żݺ??ķֽ??',
  `gift_integration` int(11) NULL DEFAULT NULL COMMENT '???ͻ??',
  `gift_growth` int(11) NULL DEFAULT NULL COMMENT '???ͳɳ?ֵ',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '????????Ϣ' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_item
-- ----------------------------
INSERT INTO `oms_order_item` VALUES (1, NULL, '202512172342205352001317043652702209', 15, '华为谷雨', 'https://msb-mall-zyh2.oss-cn-shanghai.aliyuncs.com/2025-12-04//f5a25229-46d0-4c90-8d33-c8680e2f2d1d_QQ浏览器截图20241209170221.png', NULL, 225, 18, '华为谷雨 111 土豪金 128G 官方标配 优惠套装2 一年碎屏保499', 'https://msb-mall-zyh2.oss-cn-shanghai.aliyuncs.com/2025-12-04//f4b4ce33-e470-4275-80b8-5a6f116dcb8e_QQ浏览器截图20241209170221.png', NULL, 12, '尺寸:111;选择颜色:土豪金;选择版本:128G;购买方式:官方标配;套装:优惠套装2;原厂服务:一年碎屏保499', NULL, NULL, NULL, NULL, 7000, 7000);

-- ----------------------------
-- Table structure for oms_order_operate_history
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_operate_history`;
CREATE TABLE `oms_order_operate_history`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_id` bigint(20) NULL DEFAULT NULL COMMENT '????id',
  `operate_man` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??????[?û???ϵͳ????̨????Ա]',
  `create_time` datetime NULL DEFAULT NULL COMMENT '????ʱ?',
  `order_status` tinyint(4) NULL DEFAULT NULL COMMENT '????״̬??0->?????1->????????2->?ѷ?????3->?????ɣ?4->?ѹرգ?5->??Ч??????',
  `note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??ע',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '??????????ʷ??¼' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_operate_history
-- ----------------------------

-- ----------------------------
-- Table structure for oms_order_return_apply
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_return_apply`;
CREATE TABLE `oms_order_return_apply`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_id` bigint(20) NULL DEFAULT NULL COMMENT 'order_id',
  `sku_id` bigint(20) NULL DEFAULT NULL COMMENT '?˻???Ʒid',
  `order_sn` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '???????',
  `create_time` datetime NULL DEFAULT NULL COMMENT '????ʱ?',
  `member_username` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??Ա?û???',
  `return_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '?˿??',
  `return_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?˻???????',
  `return_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?˻??˵绰',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '????״̬[0->?????���1->?˻??У?2->?????ɣ?3->?Ѿܾ?]',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '????ʱ?',
  `sku_img` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??ƷͼƬ',
  `sku_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??Ʒ???',
  `sku_brand` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??ƷƷ?',
  `sku_attrs_vals` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??Ʒ????????(JSON)',
  `sku_count` int(11) NULL DEFAULT NULL COMMENT '?˻?????',
  `sku_price` decimal(18, 4) NULL DEFAULT NULL COMMENT '??Ʒ???',
  `sku_real_price` decimal(18, 4) NULL DEFAULT NULL COMMENT '??Ʒʵ??֧?????',
  `reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ԭ?',
  `description??` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '????',
  `desc_pics` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'ƾ֤ͼƬ???Զ??Ÿ',
  `handle_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '???���ע',
  `handle_man` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??????Ա',
  `receive_man` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?ջ??',
  `receive_time` datetime NULL DEFAULT NULL COMMENT '?ջ?ʱ?',
  `receive_note` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?ջ???ע',
  `receive_phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?ջ??绰',
  `company_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '??˾?ջ???ַ',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?????˻????' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_return_apply
-- ----------------------------

-- ----------------------------
-- Table structure for oms_order_return_reason
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_return_reason`;
CREATE TABLE `oms_order_return_reason`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?˻?ԭ?',
  `sort` int(11) NULL DEFAULT NULL COMMENT '???',
  `status` tinyint(1) NULL DEFAULT NULL COMMENT '????״̬',
  `create_time` datetime NULL DEFAULT NULL COMMENT 'create_time',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?˻?ԭ?' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_return_reason
-- ----------------------------

-- ----------------------------
-- Table structure for oms_order_setting
-- ----------------------------
DROP TABLE IF EXISTS `oms_order_setting`;
CREATE TABLE `oms_order_setting`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `flash_order_overtime` int(11) NULL DEFAULT NULL COMMENT '??ɱ??????ʱ?ر?ʱ??(??)',
  `normal_order_overtime` int(11) NULL DEFAULT NULL COMMENT '??????????ʱʱ??(??)',
  `confirm_overtime` int(11) NULL DEFAULT NULL COMMENT '???????Զ?ȷ???ջ?ʱ?䣨?죩',
  `finish_overtime` int(11) NULL DEFAULT NULL COMMENT '?Զ????ɽ???ʱ?䣬?????????˻????죩',
  `comment_overtime` int(11) NULL DEFAULT NULL COMMENT '???????ɺ??Զ?????ʱ?䣨?죩',
  `member_level` tinyint(2) NULL DEFAULT NULL COMMENT '??Ա?ȼ???0-???޻?Ա?ȼ???ȫ??ͨ?ã?????-??Ӧ????????Ա?ȼ???',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '??????????Ϣ' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_order_setting
-- ----------------------------

-- ----------------------------
-- Table structure for oms_payment_info
-- ----------------------------
DROP TABLE IF EXISTS `oms_payment_info`;
CREATE TABLE `oms_payment_info`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_sn` char(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?????ţ?????ҵ???ţ?',
  `order_id` bigint(20) NULL DEFAULT NULL COMMENT '????id',
  `alipay_trade_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '֧??????????ˮ?',
  `total_amount` decimal(18, 4) NULL DEFAULT NULL COMMENT '֧???ܽ',
  `subject` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '???????',
  `payment_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '֧??״̬',
  `create_time` datetime NULL DEFAULT NULL COMMENT '????ʱ?',
  `confirm_time` datetime NULL DEFAULT NULL COMMENT 'ȷ??ʱ?',
  `callback_content` varchar(4000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?ص????',
  `callback_time` datetime NULL DEFAULT NULL COMMENT '?ص?ʱ?',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '֧????Ϣ?' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_payment_info
-- ----------------------------

-- ----------------------------
-- Table structure for oms_refund_info
-- ----------------------------
DROP TABLE IF EXISTS `oms_refund_info`;
CREATE TABLE `oms_refund_info`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'id',
  `order_return_id` bigint(20) NULL DEFAULT NULL COMMENT '?˿??Ķ???',
  `refund` decimal(18, 4) NULL DEFAULT NULL COMMENT '?˿??',
  `refund_sn` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '?˿????ˮ?',
  `refund_status` tinyint(1) NULL DEFAULT NULL COMMENT '?˿?״̬',
  `refund_channel` tinyint(4) NULL DEFAULT NULL COMMENT '?˿?????[1-֧??????2-΢?ţ?3-??????4-???',
  `refund_content` varchar(5000) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT = '?˿???Ϣ' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oms_refund_info
-- ----------------------------

-- ----------------------------
-- Table structure for undo_log
-- ----------------------------
DROP TABLE IF EXISTS `undo_log`;
CREATE TABLE `undo_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `branch_id` bigint(20) NOT NULL,
  `xid` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `context` varchar(128) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `rollback_info` longblob NOT NULL,
  `log_status` int(11) NOT NULL,
  `log_created` datetime NOT NULL,
  `log_modified` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `ux_undo_log`(`xid`, `branch_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of undo_log
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
