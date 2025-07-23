// ========================================
// SAFENET SHIELD - FIREBASE FUNCTIONS SECURITY
// ========================================

const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Security middleware for function calls
const securityMiddleware = {
  
  // Verify user authentication
  requireAuth: (req, res, next) => {
    if (!req.user) {
      return res.status(401).json({ error: 'Authentication required' });
    }
    next();
  },
  
  // Verify email verification
  requireVerifiedEmail: (req, res, next) => {
    if (!req.user.email_verified) {
      return res.status(403).json({ error: 'Email verification required' });
    }
    next();
  },
  
  // Check user roles
  requireRole: (roles) => {
    return (req, res, next) => {
      const userRole = req.user.role;
      if (!roles.includes(userRole)) {
        return res.status(403).json({ error: 'Insufficient permissions' });
      }
      next();
    };
  },
  
  // Rate limiting
  rateLimit: (maxRequests, windowMs) => {
    const requests = new Map();
    return (req, res, next) => {
      const userId = req.user.uid;
      const now = Date.now();
      const userRequests = requests.get(userId) || [];
      
      // Clean old requests
      const recentRequests = userRequests.filter(time => now - time < windowMs);
      
      if (recentRequests.length >= maxRequests) {
        return res.status(429).json({ error: 'Rate limit exceeded' });
      }
      
      recentRequests.push(now);
      requests.set(userId, recentRequests);
      next();
    };
  },
  
  // Validate request data
  validateSchema: (schema) => {
    return (req, res, next) => {
      const { error } = schema.validate(req.body);
      if (error) {
        return res.status(400).json({ error: error.details[0].message });
      }
      next();
    };
  }
};

// Function-specific security rules
const functionSecurity = {
  
  // Report submission functions
  submitReport: [
    securityMiddleware.requireAuth,
    securityMiddleware.requireVerifiedEmail,
    securityMiddleware.rateLimit(10, 60000), // 10 requests per minute
    securityMiddleware.validateSchema(reportSchema)
  ],
  
  // Government API functions
  submitGovernmentReport: [
    securityMiddleware.requireAuth,
    securityMiddleware.requireVerifiedEmail,
    securityMiddleware.requireRole(['user', 'premium_user']),
    securityMiddleware.rateLimit(5, 300000), // 5 requests per 5 minutes
    securityMiddleware.validateSchema(governmentReportSchema)
  ],
  
  // Law enforcement functions
  accessEvidenceData: [
    securityMiddleware.requireAuth,
    securityMiddleware.requireRole(['law_enforcement', 'prosecutor', 'judge']),
    securityMiddleware.rateLimit(100, 60000), // 100 requests per minute
    auditLogMiddleware
  ],
  
  // Administrative functions
  manageUsers: [
    securityMiddleware.requireAuth,
    securityMiddleware.requireRole(['admin']),
    securityMiddleware.rateLimit(50, 60000),
    auditLogMiddleware
  ],
  
  // AI analysis functions
  analyzeContent: [
    securityMiddleware.requireAuth,
    securityMiddleware.requireVerifiedEmail,
    securityMiddleware.rateLimit(20, 60000), // 20 requests per minute
    securityMiddleware.validateSchema(contentAnalysisSchema)
  ],
  
  // Blockchain functions
  verifyEvidence: [
    securityMiddleware.requireAuth,
    securityMiddleware.requireVerifiedEmail,
    securityMiddleware.rateLimit(10, 60000),
    blockchainValidationMiddleware
  ],
  
  // Emergency functions
  triggerEmergency: [
    securityMiddleware.requireAuth,
    securityMiddleware.rateLimit(3, 300000), // 3 emergency calls per 5 minutes
    emergencyValidationMiddleware
  ],
  
  // Analytics functions
  generateReport: [
    securityMiddleware.requireAuth,
    securityMiddleware.requireRole(['admin', 'analyst', 'compliance_officer']),
    securityMiddleware.rateLimit(10, 300000) // 10 reports per 5 minutes
  ]
};

// Custom middleware implementations
const auditLogMiddleware = (req, res, next) => {
  // Log all administrative and law enforcement actions
  const auditData = {
    userId: req.user.uid,
    action: req.path,
    timestamp: admin.firestore.FieldValue.serverTimestamp(),
    ip: req.ip,
    userAgent: req.get('User-Agent'),
    requestData: sanitizeData(req.body)
  };
  
  admin.firestore().collection('audit_trail').add(auditData)
    .then(() => next())
    .catch(error => {
      console.error('Audit logging failed:', error);
      next(); // Continue execution even if audit fails
    });
};

const blockchainValidationMiddleware = (req, res, next) => {
  // Validate blockchain evidence integrity
  const { evidenceHash, blockHash } = req.body;
  
  // Perform blockchain validation
  validateBlockchainEvidence(evidenceHash, blockHash)
    .then(isValid => {
      if (!isValid) {
        return res.status(400).json({ error: 'Invalid blockchain evidence' });
      }
      next();
    })
    .catch(error => {
      console.error('Blockchain validation failed:', error);
      res.status(500).json({ error: 'Validation error' });
    });
};

const emergencyValidationMiddleware = (req, res, next) => {
  // Validate emergency request legitimacy
  const { location, urgency, contactInfo } = req.body;
  
  if (!location || !urgency || !contactInfo) {
    return res.status(400).json({ error: 'Missing emergency data' });
  }
  
  // Additional validation logic for emergency requests
  validateEmergencyRequest(req.body)
    .then(isValid => {
      if (!isValid) {
        return res.status(400).json({ error: 'Invalid emergency request' });
      }
      next();
    })
    .catch(error => {
      console.error('Emergency validation failed:', error);
      res.status(500).json({ error: 'Validation error' });
    });
};

// Data sanitization
const sanitizeData = (data) => {
  // Remove sensitive information from audit logs
  const sensitiveFields = ['password', 'token', 'secret', 'key', 'credential'];
  const sanitized = { ...data };
  
  for (const field of sensitiveFields) {
    if (sanitized[field]) {
      sanitized[field] = '[REDACTED]';
    }
  }
  
  return sanitized;
};

// Export security configuration
module.exports = {
  securityMiddleware,
  functionSecurity,
  auditLogMiddleware,
  blockchainValidationMiddleware,
  emergencyValidationMiddleware
};
