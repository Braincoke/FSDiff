package core;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Generates hashes of a file and stores them in a HashedFile object
 */
public class HashGenerator {

    private boolean md5 = true;
    private boolean sha1 = false;
    private boolean sha256 = false;

    public HashGenerator(boolean md5, boolean sha1, boolean sha256){
        this.md5 = md5;
        this.sha1 = sha1;
        this.sha256 = sha256;
    }

    /** Hash a file given the algorithm and outputs a hexadecimal string representing the hash
     *
     * @param file      the input file to hash
     * @param algorithm the algorithm to use to hash the file (md5, sha-1 ...)
     * @return          a string representing the hash in hexadecimal
     * @throws HashGenerationException
     */
    private static String hashFile(Path file, String algorithm)
            throws HashGenerationException {
        try {
            FileInputStream inputStream = new FileInputStream(file.toFile());
            MessageDigest digest = MessageDigest.getInstance(algorithm);

            byte[] bytesBuffer = new byte[1024];
            int bytesRead = -1;

            while ((bytesRead = inputStream.read(bytesBuffer)) != -1) {
                digest.update(bytesBuffer, 0, bytesRead);
            }

            byte[] hashedBytes = digest.digest();

            return convertByteArrayToHexString(hashedBytes);

        } catch (FileNotFoundException e) {
            throw new HashGenerationException(
                    "File not found", e);
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return "Error";
    }

    /** Converts a byte array to a string representing the byte array in hexadecimal
     *
     * @param arrayBytes the byte array to convert to a hexadecimal string
     * @return the string representing the byte array, in hexadecimal
     */
    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuilder stringBuffer = new StringBuilder();
        for (byte arrayByte : arrayBytes) {
            stringBuffer.append(Integer.toString((arrayByte & 0xff) + 0x100, 16)
                    .substring(1));
        }
        return stringBuffer.toString();
    }

    /** Generates the MD5 hash of the given file
     *
     * @param file
     * @return the hash as a string in hexadecimal
     * @throws HashGenerationException
     */
    public static String generateMD5(Path file) throws HashGenerationException {
        return hashFile(file, "MD5");
    }

    /** Generates the SHA-1 hash of the given file
     *
     * @param file
     * @return the hash as a string in hexadecimal
     * @throws HashGenerationException
     */
    public static String generateSHA1(Path file) throws HashGenerationException {
        return hashFile(file, "SHA-1");
    }

    /** Generates the SHA-256 hash of the given file
     *
     * @param file
     * @return the hash as a string in hexadecimal
     * @throws HashGenerationException
     */
    public static String generateSHA256(Path file) throws HashGenerationException {
        return hashFile(file, "SHA-256");
    }

    /** Generate MD5, SHA1, SH256 hashes of a file in that order
     *
     * @param file The file to be hashed
     * @return A list of the hashes of the files [MD5, SHA1, SHA256]
     * @throws HashGenerationException
     */
    public static ArrayList<String> generateHashesList(Path file) throws HashGenerationException {
        ArrayList<String> hashes = new ArrayList<>();
        hashes.add(0, generateMD5(file));
        hashes.add(0, generateSHA1(file));
        hashes.add(0, generateSHA256(file));
        return hashes;
    }

    public static HashedFile generateHashedFile(Path file) throws HashGenerationException {
        HashedFile hashedFile = new HashedFile(file);
        hashedFile.setMd5(generateMD5(file));
        return hashedFile;
    }

    public HashedFile getHashedFile(Path file) throws HashGenerationException {
        HashedFile hashedFile = new HashedFile(file);
        if(md5)
            hashedFile.setMd5(generateMD5(file));
        if(sha1)
            hashedFile.setSha1(generateSHA1(file));
        if(sha256)
            hashedFile.setSha256(generateSHA256(file));
        return hashedFile;
    }
}
