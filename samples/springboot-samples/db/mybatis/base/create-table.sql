
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(10) NOT NULL,
  `password` varchar(10) NOT NULL,
  `age` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb3;

-- ----------------------------
-- Records of user
-- ----------------------------
BEGIN;
INSERT INTO `user` (`id`, `name`, `password`, `age`) VALUES (2, '沉默王三', '123456', 18);
INSERT INTO `user` (`id`, `name`, `password`, `age`) VALUES (3, '沉默王四', '123456', 18);
INSERT INTO `user` (`id`, `name`, `password`, `age`) VALUES (4, '沉默王二', '123456', 18);
INSERT INTO `user` (`id`, `name`, `password`, `age`) VALUES (5, '沉默王三', '123456', 18);
INSERT INTO `user` (`id`, `name`, `password`, `age`) VALUES (6, '沉默王四', '123456', 18);
INSERT INTO `user` (`id`, `name`, `password`, `age`) VALUES (7, '沉默王二', '123456', 18);
INSERT INTO `user` (`id`, `name`, `password`, `age`) VALUES (8, '沉默王三', '123456', 18);
INSERT INTO `user` (`id`, `name`, `password`, `age`) VALUES (9, '沉默王四', '123456', 18);
COMMIT;

SET FOREIGN_KEY_CHECKS = 1;
