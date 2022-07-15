package com.ms.imapi2;

import com4j.*;

/**
 * <p>
 * Physical type of the optical media
 * </p>
 */
public enum IMAPI_MEDIA_PHYSICAL_TYPE implements ComEnum {
  /**
   * <p>
   * Media not present or unrecognized
   * </p>
   * <p>
   * The value of this constant is 0
   * </p>
   */
  IMAPI_MEDIA_TYPE_UNKNOWN(0),
  /**
   * <p>
   * CD-ROM media
   * </p>
   * <p>
   * The value of this constant is 1
   * </p>
   */
  IMAPI_MEDIA_TYPE_CDROM(1),
  /**
   * <p>
   * CD-R media
   * </p>
   * <p>
   * The value of this constant is 2
   * </p>
   */
  IMAPI_MEDIA_TYPE_CDR(2),
  /**
   * <p>
   * CD-RW media
   * </p>
   * <p>
   * The value of this constant is 3
   * </p>
   */
  IMAPI_MEDIA_TYPE_CDRW(3),
  /**
   * <p>
   * DVD-ROM media
   * </p>
   * <p>
   * The value of this constant is 4
   * </p>
   */
  IMAPI_MEDIA_TYPE_DVDROM(4),
  /**
   * <p>
   * DVD-RAM media
   * </p>
   * <p>
   * The value of this constant is 5
   * </p>
   */
  IMAPI_MEDIA_TYPE_DVDRAM(5),
  /**
   * <p>
   * DVD+R media
   * </p>
   * <p>
   * The value of this constant is 6
   * </p>
   */
  IMAPI_MEDIA_TYPE_DVDPLUSR(6),
  /**
   * <p>
   * DVD+RW media
   * </p>
   * <p>
   * The value of this constant is 7
   * </p>
   */
  IMAPI_MEDIA_TYPE_DVDPLUSRW(7),
  /**
   * <p>
   * DVD+R dual layer media
   * </p>
   * <p>
   * The value of this constant is 8
   * </p>
   */
  IMAPI_MEDIA_TYPE_DVDPLUSR_DUALLAYER(8),
  /**
   * <p>
   * DVD-R media
   * </p>
   * <p>
   * The value of this constant is 9
   * </p>
   */
  IMAPI_MEDIA_TYPE_DVDDASHR(9),
  /**
   * <p>
   * DVD-RW media
   * </p>
   * <p>
   * The value of this constant is 10
   * </p>
   */
  IMAPI_MEDIA_TYPE_DVDDASHRW(10),
  /**
   * <p>
   * DVD-R dual layer media
   * </p>
   * <p>
   * The value of this constant is 11
   * </p>
   */
  IMAPI_MEDIA_TYPE_DVDDASHR_DUALLAYER(11),
  /**
   * <p>
   * Randomly writable media
   * </p>
   * <p>
   * The value of this constant is 12
   * </p>
   */
  IMAPI_MEDIA_TYPE_DISK(12),
  /**
   * <p>
   * DVD+RW dual layer media
   * </p>
   * <p>
   * The value of this constant is 13
   * </p>
   */
  IMAPI_MEDIA_TYPE_DVDPLUSRW_DUALLAYER(13),
  /**
   * <p>
   * HD DVD-ROM media
   * </p>
   * <p>
   * The value of this constant is 14
   * </p>
   */
  IMAPI_MEDIA_TYPE_HDDVDROM(14),
  /**
   * <p>
   * HD DVD-R media
   * </p>
   * <p>
   * The value of this constant is 15
   * </p>
   */
  IMAPI_MEDIA_TYPE_HDDVDR(15),
  /**
   * <p>
   * HD DVD-RAM media
   * </p>
   * <p>
   * The value of this constant is 16
   * </p>
   */
  IMAPI_MEDIA_TYPE_HDDVDRAM(16),
  /**
   * <p>
   * BD-ROM media
   * </p>
   * <p>
   * The value of this constant is 17
   * </p>
   */
  IMAPI_MEDIA_TYPE_BDROM(17),
  /**
   * <p>
   * BD-R media
   * </p>
   * <p>
   * The value of this constant is 18
   * </p>
   */
  IMAPI_MEDIA_TYPE_BDR(18),
  /**
   * <p>
   * BD-RE media
   * </p>
   * <p>
   * The value of this constant is 19
   * </p>
   */
  IMAPI_MEDIA_TYPE_BDRE(19),
  /**
   * <p>
   * Max value for a media type
   * </p>
   * <p>
   * The value of this constant is 19
   * </p>
   */
  IMAPI_MEDIA_TYPE_MAX(19),
  ;

  private final int value;
  IMAPI_MEDIA_PHYSICAL_TYPE(int value) { this.value=value; }
  public int comEnumValue() { return value; }
}
