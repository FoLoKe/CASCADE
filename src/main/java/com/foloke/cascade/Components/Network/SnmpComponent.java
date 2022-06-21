package com.foloke.cascade.Components.Network;

import com.badlogic.ashley.core.Component;
import org.snmp4j.Target;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.UdpAddress;

public class SnmpComponent implements Component {
    //SNMP
    public Target<UdpAddress> target;
    public UsmUser user;
    String snmpAddress = "127.0.0.1";
    String snmpPort = "161";
    int snmpVersion = SnmpConstants.version2c;
    int snmpTimeout = 5000;
    String snmpName = "public";

    //SNMPv3
    String snmpPassword = "12345678";
    OID authProtocol = AuthMD5.ID;
    OID encryptionProtocol = PrivDES.ID;
    String snmpEncryptionPass = "12345678";
    int securityLevel = SecurityLevel.NOAUTH_NOPRIV;
}
