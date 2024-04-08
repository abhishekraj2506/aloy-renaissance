package com.aloy.coreapp.utils;

import jakarta.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.*;
import java.util.Arrays;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class GenericUtils {

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom secureRandom = new SecureRandom();

    public static boolean isValidPhoneNumber(String phoneNumber) {
        //[7-9]: starting of the number may contain a digit between 7 to 9
        //[0-9]: then contains digits 0 to 9
        Pattern ptrn = Pattern.compile("^[6-9][0-9]{9}");
        //the matcher() method creates a matcher that will match the given input against this pattern
        Matcher match = ptrn.matcher(phoneNumber);
        //returns a boolean value
        return (match.find() && match.group().equals(phoneNumber));
    }

    public static int generateRandomNumber(int length) {
        return ThreadLocalRandom.current().nextInt((int) Math.pow(10, length - 1), (int) Math.pow(10, length));
    }
    public static String generateRandomAlphaNumericString(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int randomIndex = secureRandom.nextInt(ALPHA_NUMERIC_STRING.length());
            char randomChar = ALPHA_NUMERIC_STRING.charAt(randomIndex);
            builder.append(randomChar);
        }

        return builder.toString();
    }

    public static OffsetDateTime getOffsetDateTime(Date date) {
        return OffsetDateTime.ofInstant(date.toInstant(), ZoneOffset.UTC);
    }

    public static OffsetDateTime getOffsetDateTime(String timestamp) {
        return getOffsetDateTime(Long.valueOf(timestamp));
    }

    public static OffsetDateTime getOffsetDateTime(Long timestamp) {
        //convert seconds to millis
        if (String.valueOf(timestamp).length() == 10) {
            timestamp = timestamp * 1000;
        }
        return Instant.ofEpochMilli(timestamp).atOffset(ZoneOffset.UTC);
    }

    public static File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convFile);
        fos.write(file.getBytes());
        fos.close();
        return convFile;
    }

    public static String getHashedString(String stringToHash) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        md.update(stringToHash.getBytes());
        byte[] digest = md.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }

    public static UUID generateUuid() {
        return UUID.randomUUID();
    }

    public static UUID getUuidFromString(String uuid) {
        return UUID.fromString(uuid);
    }

    private static String uuidToBase64(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return Base64.encodeBase64URLSafeString(bb.array());
    }

    private static UUID uuidFromBase64(String str) {
        byte[] bytes = Base64.decodeBase64(str);
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        return new UUID(bb.getLong(), bb.getLong());
    }

    private static Key getSecret(final String myKey) throws Exception {
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            log.error(ExceptionUtils.getStackTrace(e));
            throw new Exception("Failed to create secret encryption key.");
        }
    }

    public static String encrypt(final String strToEncrypt, final String secret) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, getSecret(secret));
            return Base64.encodeBase64URLSafeString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            log.error("Error while encrypting: " + ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    public static String decrypt(final String strToDecrypt, final String secret) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, getSecret(secret));
            return new String(cipher.doFinal(Base64.decodeBase64(strToDecrypt)));
        } catch (Exception e) {
            log.error("Error while decrypting: " + ExceptionUtils.getStackTrace(e));
            return null;
        }
    }

    public static Long getStartOfDayMillis(Long timeMillis) {
        // Define the time zone (India Standard Time)
        ZoneId indiaTimeZone = ZoneId.of("Asia/Kolkata");

        // Get the current date in India time zone
        LocalDate currentDate = LocalDate.ofInstant(Instant.ofEpochMilli(timeMillis), indiaTimeZone);

        // Get the start of the day in India time zone
        ZonedDateTime startOfDayInIndia = currentDate.atStartOfDay(indiaTimeZone);

        // Convert the start of the day to UTC
        return startOfDayInIndia.toOffsetDateTime().withOffsetSameInstant(ZoneOffset.UTC)
                .toInstant().toEpochMilli();

    }

    public static String getMaskedPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) return null;
        return "XXX-XXX-".concat(phoneNumber.substring(6));
    }
}
